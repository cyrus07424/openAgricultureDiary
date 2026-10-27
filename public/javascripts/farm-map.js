(function () {
    'use strict';

    const basemaps = {
        pale: { tiles: ['https://cyberjapandata.gsi.go.jp/xyz/pale/{z}/{x}/{y}.png'], maxzoom: 18, attribution: '<a href="https://maps.gsi.go.jp/development/ichiran.html">国土地理院</a>' },
        standard: { tiles: ['https://cyberjapandata.gsi.go.jp/xyz/std/{z}/{x}/{y}.png'], maxzoom: 18, attribution: '<a href="https://maps.gsi.go.jp/development/ichiran.html">国土地理院</a>' },
        photo: { tiles: ['https://cyberjapandata.gsi.go.jp/xyz/seamlessphoto/{z}/{x}/{y}.jpg'], maxzoom: 18, attribution: '<a href="https://maps.gsi.go.jp/development/ichiran.html">国土地理院・空中写真</a>' },
        osm: { tiles: ['https://tile.openstreetmap.org/{z}/{x}/{y}.png'], maxzoom: 19, attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors' }
    };

    function validBounds(values) {
        if (values.some(value => value === '' || value === null || value === undefined)) return null;
        const [north, east, south, west] = values.map(Number);
        if (![north, east, south, west].every(Number.isFinite) ||
            south < -85.051129 || north > 85.051129 || west < -180 || east > 180 ||
            north <= south || east <= west) return null;
        return [north, east, south, west];
    }

    function feature(bounds, properties) {
        const [north, east, south, west] = bounds;
        return {
            type: 'Feature', properties,
            geometry: { type: 'Polygon', coordinates: [[[west, south], [east, south], [east, north], [west, north], [west, south]]] }
        };
    }

    function style(selected) {
        const sources = {};
        const layers = [];
        Object.entries(basemaps).forEach(([id, source]) => {
            sources[id] = Object.assign({ type: 'raster', tileSize: 256 }, source);
            layers.push({ id, type: 'raster', source: id, layout: { visibility: id === selected ? 'visible' : 'none' } });
        });
        return { version: 8, sources, layers };
    }

    function init(panel) {
        const status = panel.querySelector('.map-status');
        const container = panel.querySelector('.farm-map');
        const select = panel.querySelector('.map-basemap');
        const fitButton = panel.querySelector('.map-fit');
        const drawButton = panel.querySelector('.map-draw');
        const editable = panel.dataset.editable === 'true';
        const inputNames = ['northEastLatitude', 'northEastLongitude', 'southWestLatitude', 'southWestLongitude'];
        const inputs = editable ? inputNames.map(name => panel.closest('form').querySelector('[name="' + name + '"]')) : [];
        let drawing = false;
        let first = null;
        let cornerMarker = null;
        let loaded = false;
        let tileError = false;
        let features = [];
        let skipped = 0;
        let map;
        const message = key => { status.textContent = panel.dataset[key]; };
        function clearCorner() {
            first = null;
            if (cornerMarker) cornerMarker.remove();
            cornerMarker = null;
        }

        if (!window.maplibregl) {
            message('unavailable');
            select.disabled = fitButton.disabled = true;
            if (drawButton) drawButton.disabled = true;
            return;
        }
        panel.querySelectorAll('.map-field-data > span').forEach(element => {
            const data = element.dataset;
            const bounds = validBounds([data.neLat, data.neLng, data.swLat, data.swLng]);
            if (bounds) features.push(feature(bounds, { name: data.name, url: data.url }));
            else skipped += 1;
        });
        function fit() {
            if (!features.length) return;
            const bounds = new window.maplibregl.LngLatBounds();
            features.forEach(item => item.geometry.coordinates[0].forEach(point => bounds.extend(point)));
            map.fitBounds(bounds, { padding: 55, maxZoom: 16, duration: 0 });
        }
        function updateSource() {
            if (loaded) map.getSource('fields').setData({ type: 'FeatureCollection', features });
        }
        function syncInputs(zoom) {
            const bounds = validBounds(inputs.map(input => input.value));
            features = bounds ? [feature(bounds, {})] : [];
            updateSource();
            message(bounds ? 'selected' : (inputs.some(input => input.value !== '') ? 'invalid' : 'first'));
            if (zoom && bounds) fit();
        }
        try {
            map = new window.maplibregl.Map({
                container, style: style(select.value),
                center: [138.2, 36.2], zoom: 5, maxZoom: 20,
                cooperativeGestures: true
            });
        } catch (error) {
            console.error('MapLibre initialization failed', error);
            message('unavailable');
            select.disabled = fitButton.disabled = true;
            if (drawButton) drawButton.disabled = true;
            return;
        }
        map.addControl(new window.maplibregl.NavigationControl(), 'top-right');
        map.addControl(new window.maplibregl.GeolocateControl({
            positionOptions: { enableHighAccuracy: true }, fitBoundsOptions: { maxZoom: 16 }
        }), 'top-right');
        map.addControl(new window.maplibregl.ScaleControl({ unit: 'metric' }));
        select.disabled = fitButton.disabled = true;
        if (drawButton) drawButton.disabled = true;
        map.on('error', event => {
            console.error('MapLibre resource error', event.error);
            tileError = true;
            message('error');
        });
        map.on('load', () => {
            loaded = true;
            select.disabled = fitButton.disabled = false;
            if (drawButton) drawButton.disabled = false;
            map.addSource('fields', { type: 'geojson', data: { type: 'FeatureCollection', features } });
            map.addLayer({ id: 'field-fill', type: 'fill', source: 'fields', paint: { 'fill-color': '#16866a', 'fill-opacity': 0.27 } });
            map.addLayer({ id: 'field-outline', type: 'line', source: 'fields', paint: { 'line-color': '#087353', 'line-width': 3 } });
            if (editable) syncInputs(true);
            else {
                fit();
                message(skipped ? 'skipped' : (features.length ? 'ready' : 'empty'));
            }
            if (tileError) message('error');
        });
        select.addEventListener('change', () => {
            tileError = false;
            Object.keys(basemaps).forEach(id => map.setLayoutProperty(id, 'visibility', id === select.value ? 'visible' : 'none'));
            message('loading');
            map.once('idle', () => {
                if (!tileError) message(drawing ? (first ? 'second' : 'first') : (skipped ? 'skipped' : (features.length ? 'ready' : 'empty')));
            });
        });
        fitButton.addEventListener('click', fit);
        map.on('click', event => {
            if (editable) {
                if (!drawing || !loaded) return;
                if (!first) {
                    first = event.lngLat;
                    cornerMarker = new window.maplibregl.Marker({ color: '#b17c13' }).setLngLat(first).addTo(map);
                    message('second');
                } else {
                    const bounds = validBounds([
                        Math.max(first.lat, event.lngLat.lat), Math.max(first.lng, event.lngLat.lng),
                        Math.min(first.lat, event.lngLat.lat), Math.min(first.lng, event.lngLat.lng)
                    ]);
                    if (!bounds) { message('invalid'); return; }
                    inputs.forEach((input, index) => {
                        input.value = bounds[index].toFixed(6);
                        input.dispatchEvent(new Event('input', { bubbles: true }));
                    });
                    clearCorner();
                    drawing = false;
                    drawButton.setAttribute('aria-pressed', 'false');
                    container.classList.remove('is-drawing');
                    syncInputs(false);
                }
                return;
            }
            if (!loaded) return;
            const hit = map.queryRenderedFeatures(event.point, { layers: ['field-fill'] })[0];
            if (!hit) return;
            const content = document.createElement('div');
            const title = document.createElement('strong');
            title.textContent = hit.properties.name;
            const link = document.createElement('a');
            link.textContent = panel.dataset.open;
            // Only server-rendered, same-origin field links may be opened.
            const url = new URL(hit.properties.url, window.location.origin);
            if (url.origin !== window.location.origin) return;
            link.href = url.href;
            content.append(title, document.createElement('br'), link);
            new window.maplibregl.Popup().setLngLat(event.lngLat).setDOMContent(content).addTo(map);
        });
        if (editable) {
            drawButton.addEventListener('click', () => {
                drawing = !drawing;
                clearCorner();
                drawButton.setAttribute('aria-pressed', String(drawing));
                container.classList.toggle('is-drawing', drawing);
                if (drawing) message('first');
                else syncInputs(false);
            });
            inputs.forEach(input => input.addEventListener('input', () => {
                clearCorner();
                syncInputs(false);
            }));
            const form = panel.closest('form');
            form.addEventListener('submit', event => {
                if (!validBounds(inputs.map(input => input.value))) {
                    event.preventDefault();
                    message('invalid');
                    inputs[0].focus();
                }
            }, true);
        }
        if (window.ResizeObserver) new ResizeObserver(() => map.resize()).observe(container);
    }

    if (typeof module !== 'undefined' && module.exports) module.exports = { validBounds, feature, style };
    if (typeof document !== 'undefined') {
        document.addEventListener('DOMContentLoaded', () => document.querySelectorAll('[data-farm-map]').forEach(init));
    }
}());
