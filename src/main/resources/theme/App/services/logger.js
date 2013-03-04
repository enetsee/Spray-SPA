define(['durandal/system'],
    function (system) {
        var logger = {
            log: log,
            logError: logError
        };

        return logger;

        function log(message, data, source) {
            logIt(message, data, source);
        }

        function logError(message, data, source) {
            logIt(message, data, source);
        }

        function logIt(message, data, source) {
            source = source ? '[' + source + '] ' : '';
            if (data) {
                system.log(source, message, data);
            } else {
                system.log(source, message);
            }
        }
    });