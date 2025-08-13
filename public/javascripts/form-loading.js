/**
 * Form submission loading modal to prevent double-clicking
 * Shows appropriate Japanese loading messages based on form action
 */
(function() {
    'use strict';
    
    // Map of form actions to appropriate Japanese loading messages
    const loadingMessages = {
        // Crop management
        '/crops': '作物を作成中です...',
        '/crops/': '作物を更新中です...',
        '/delete': '削除中です...',
        
        // Field management  
        '/fields': '圃場を作成中です...',
        '/fields/': '圃場を更新中です...',
        
        // Work history
        '/work-history': '作業履歴を作成中です...',
        '/work-history/': '作業履歴を更新中です...',
        
        // Authentication
        '/login': 'ログイン中です...',
        '/register': '登録中です...',
        '/forgot-password': 'メール送信中です...',
        '/reset-password': 'パスワード変更中です...',
        
        // Pesticide management
        '/pesticides/upload': 'アップロード中です...',
        '/pesticides/clear': '削除中です...',
        
        // Default fallback
        'default': '処理中です...'
    };
    
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
        
        // Check if it's a delete operation
        if (action.includes('/delete') || form.querySelector('button[type="submit"]')?.textContent?.includes('削除')) {
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