define(['services/logger','durandal/app'], function (logger,app) {
    var AccountModal= function() {
        this.input = ko.observable('');
    }
    AccountModal.prototype.ok = function() {
        this.modal.close(this.input());
    };
    return AccountModal
});