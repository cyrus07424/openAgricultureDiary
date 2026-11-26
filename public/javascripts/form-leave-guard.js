(function() {
    'use strict';

    function getFormState(form) {
        return JSON.stringify(Array.from(form.querySelectorAll('input, select, textarea'))
            .filter(function(field) {
                return field.type !== 'hidden';
            })
            .map(function(field) {
                if (field.type === 'checkbox' || field.type === 'radio') {
                    return field.checked;
                }
                if (field.type === 'file') {
                    return Array.from(field.files, function(file) {
                        return file.name;
                    });
                }
                if (field.tagName === 'SELECT' && field.multiple) {
                    return Array.from(field.selectedOptions, function(option) {
                        return option.value;
                    });
                }
                return field.value;
            }));
    }

    function initializeFormLeaveGuard() {
        const formStates = [];
        let isSubmitting = false;
        let navigationConfirmed = false;
        const message = document.documentElement.lang.toLowerCase().startsWith('en')
            ? 'You have unsaved changes. Are you sure you want to leave this page?'
            : '入力内容が保存されていません。このページを離れますか？';

        document.querySelectorAll('form').forEach(function(form) {
            if (form.method.toLowerCase() !== 'post' || form.action.endsWith('/login')) {
                return;
            }

            const initialState = getFormState(form);
            const state = { dirty: false };
            formStates.push(state);

            function updateDirtyState() {
                state.dirty = getFormState(form) !== initialState;
            }

            form.addEventListener('input', updateDirtyState);
            form.addEventListener('change', updateDirtyState);
            form.addEventListener('submit', function(event) {
                if (!event.defaultPrevented) {
                    isSubmitting = true;
                }
            });
        });

        function hasDirtyForm() {
            return formStates.some(function(state) {
                return state.dirty;
            });
        }

        window.addEventListener('beforeunload', function(event) {
            if (!isSubmitting && !navigationConfirmed && hasDirtyForm()) {
                event.preventDefault();
                event.returnValue = '';
            }
        });

        document.addEventListener('click', function(event) {
            if (event.defaultPrevented || event.button !== 0 || event.ctrlKey ||
                event.metaKey || event.shiftKey || event.altKey || isSubmitting || !hasDirtyForm()) {
                return;
            }

            const link = event.target.closest('a');
            if (!link) return;

            const href = (link.getAttribute('href') || '').trim();
            if (!href || /^(#|javascript:)/i.test(href) ||
                link.target.toLowerCase() === '_blank' || link.hasAttribute('download')) {
                return;
            }

            if (!window.confirm(message)) {
                event.preventDefault();
                return;
            }

            // The confirmed link must not trigger a second, native confirmation.
            // beforeunload runs while the navigation starts; re-arm the guard
            // after a short delay in case the navigation does not happen.
            navigationConfirmed = true;
            setTimeout(function() {
                navigationConfirmed = false;
            }, 1000);
        });
    }

    document.addEventListener('DOMContentLoaded', initializeFormLeaveGuard);
})();
