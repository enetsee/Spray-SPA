require.config({
    paths: { "text": "durandal/amd/text" }
});

define(['durandal/app', 'durandal/viewLocator', 'durandal/system', 'durandal/plugins/router', 'services/logger'],
    function (app, viewLocator, system, router, logger) {

    // Enable debug message to show in the console 
    system.debug(true);

    app.start().then(function () {

        var accountId =

        router.handleInvalidRoute = function (route, params) {
            logger.logError('No Route Found', route, 'main');
        };
        // When finding a viewmodel module, replace the viewmodel string 
        // with view to find it partner view.
        router.useConvention();
        viewLocator.useConvention();
        // Adapt to touch devices
        app.adaptToDevice();
        //Show the app by setting the root view model for our application.
        app.setRoot('viewmodels/shell','entrance');
    });
});