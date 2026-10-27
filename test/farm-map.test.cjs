const { test } = require('node:test');
const assert = require('node:assert/strict');
const { validBounds, feature, style } = require('../public/javascripts/farm-map.js');

test('bounds are numeric, finite, ordered and within Web Mercator limits', () => {
    assert.deepEqual(validBounds(['36.1', '138.1', '36', '138']), [36.1, 138.1, 36, 138]);
    assert.deepEqual(validBounds([-35, -60, -36, -61]), [-35, -60, -36, -61]);
    assert.deepEqual(validBounds([85.051129, 180, -85.051129, -180]), [85.051129, 180, -85.051129, -180]);
    for (const bounds of [
        ['', 138, 35, 137], [null, 138, 35, 137], [undefined, 138, 35, 137],
        [NaN, 138, 35, 137], [36, Infinity, 35, 137], [35, 138, 36, 137],
        [36, 138, 36, 137], [36, 138, 35, 138], [86, 138, 35, 137],
        [36, 181, 35, 137], [36, 138, -86, 137], [36, 138, 35, -181]
    ]) assert.equal(validBounds(bounds), null);
});

test('field geometry is a closed longitude-latitude rectangle', () => {
    const result = feature([36, 139, 35, 138], { name: '<test>' });
    assert.equal(result.type, 'Feature');
    assert.deepEqual(result.properties, { name: '<test>' });
    assert.deepEqual(result.geometry.coordinates, [[[138, 35], [139, 35], [139, 36], [138, 36], [138, 35]]]);
});

test('all four attributed raster basemaps retain exactly one visible layer', () => {
    for (const selected of ['pale', 'standard', 'photo', 'osm']) {
        const result = style(selected);
        assert.equal(result.version, 8);
        assert.equal(Object.keys(result.sources).length, 4);
        assert.deepEqual(result.layers.filter(layer => layer.layout.visibility === 'visible').map(layer => layer.id), [selected]);
        for (const source of Object.values(result.sources)) {
            assert.equal(source.type, 'raster');
            assert.equal(source.tileSize, 256);
            assert.match(source.tiles[0], /^https:\/\//);
            assert.ok(source.attribution.length > 10);
        }
    }
});
