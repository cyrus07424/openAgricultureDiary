/**
 * Form submission loading modal to prevent double-clicking
 * Shows appropriate loading messages based on form action
 * Now supports internationalization through data attributes
 */
(function() {
    'use strict';
    
    // Default loading messages - these will be overridden by data attributes from server
    const defaultLoadingMessages = {
        // Crop management
        '/crops': 'Creating crop...',
        '/crops/': 'Updating crop...',
        '/delete': 'Deleting...',
        
        // Field management  
        '/fields': 'Creating field...',
        '/fields/': 'Updating field...',
        
        // Work history
        '/work-history': 'Creating work entry...',
        '/work-history/': 'Updating work entry...',
        
        // Authentication
        '/login': 'Logging in...',
        '/register': 'Registering...',
        '/forgot-password': 'Sending email...',
        '/reset-password': 'Resetting password...',
        
        // Pesticide management
        '/pesticides/upload': 'Uploading...',
        '/pesticides/clear': 'Deleting...',
        
        // Default fallback
        'default': 'Processing...'
    };
    
    // Try to get localized messages from a global variable set by the server
    const loadingMessages = window.loadingMessages || defaultLoadingMessages;
    
    /**
     * Get appropriate loading message based on form action
     */
    function getLoadingMessage(form) {
        const action = form.getAttribute('action') || '';
        const method = form.getAttribute('method') || 'GET';
        
        // Check for specific patterns
        for (const pattern in loadingMessages) {
            if (pattern !== 'default' && action.includes(pattern)) {
                return loadingMessages[pattern];
            }
        }
        
        // Check if it's a delete operation (check both action and button text)
        if (action.includes('/delete') || form.querySelector('button[type="submit"]')?.textContent?.includes('削除') || form.querySelector('button[type="submit"]')?.textContent?.includes('Delete')) {
            return loadingMessages['/delete'];
        }
        
        return loadingMessages.default;
    }
    
    /**
     * Show loading modal
     */
    function showLoadingModal(message) {
        const modal = document.getElementById('loadingModal');
        const messageElement = document.getElementById('loadingMessage');
        
        if (modal && messageElement) {
            messageElement.textContent = message;
            const bootstrapModal = new bootstrap.Modal(modal, {
                backdrop: 'static',
                keyboard: false
            });
            bootstrapModal.show();
        }
    }
    
    /**
     * Initialize form submission handlers
     */
    function initializeFormHandlers() {
        const forms = document.querySelectorAll('form');
        
        forms.forEach(function(form) {
            form.addEventListener('submit', function(event) {
                const submitButton = form.querySelector('button[type="submit"]');
                
                // Prevent multiple submissions by disabling the submit button
                if (submitButton) {
                    submitButton.disabled = true;
                }
                
                // Get appropriate loading message and show modal
                const message = getLoadingMessage(form);
                showLoadingModal(message);
                
                // Re-enable button after a delay as fallback (in case of errors)
                setTimeout(function() {
                    if (submitButton) {
                        submitButton.disabled = false;
                    }
                }, 10000); // 10 seconds timeout
            });
        });
    }
    
    // Initialize when DOM is ready
    document.addEventListener('DOMContentLoaded', initializeFormHandlers);
    
    // Re-initialize if new forms are added dynamically
    window.initializeFormHandlers = initializeFormHandlers;
})();