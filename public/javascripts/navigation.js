document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('.nav-sidebar .nav-link').forEach(function (link) {
        var path = new URL(link.href).pathname;
        if (window.location.pathname === path || window.location.pathname.startsWith(path + '/')) {
            link.classList.add('active');
            link.setAttribute('aria-current', 'page');
        }
    });
});
