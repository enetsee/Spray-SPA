define(['services/logger'], function (logger) {
    var vm = {
        activate: activate,
        title: 'View 1',
    };

    return vm;

    function activate() {
        logger.log('View 1 activated', null, 'view1');
        return true;
    }
});