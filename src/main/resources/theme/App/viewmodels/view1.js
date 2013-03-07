define(['services/logger','durandal/app','viewmodels/account'], function (logger,app,Account) {

    function vm() {
        // Data
        var self = this;
        self.title = 'View 1';

        self.activate = function() {
            logger.log('View 1 activated', null, 'view1');
            return true;
        };
    };

    return (new vm());



});