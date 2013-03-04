define(['services/logger'], function (logger) {
    var vm = {
        activate: activate,
        title: 'View 2',
    };

    return vm;

    function activate() {
        logger.log('View 2 activated', null, 'view2');
        return true;
    }
});