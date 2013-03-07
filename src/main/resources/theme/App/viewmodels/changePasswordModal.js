define(['services/logger','durandal/app', 'viewmodels/account'], function (logger,app,Account) {

    var ChangePasswordModal = function() {

        // Data
        var self = this;
        self.currentPassword = ko.observable('');
        self.newPassword = ko.observable('');
        self.alerts = ko.observableArray([]);
        self.incorrectPassword = ko.observable(false);
        self.loading = ko.observable(false);


        self.activate = function() {
           logger.log('ChangePasswordModal activated', null, 'changePasswordModal');
           return true;
        };

        self.updateAlerts = function(errors) {
            self.alerts([]);
            $.map(errors , function(err) { self.alerts.push(err);} );
        }

        // Response handlers
        // 202 response handler
        self.Accepted = function(data,textStatus,jqXHR) {
            self.modal.close();
        };

        // 400 response handler
        self.BadRequest = function(jqXHR, textStatus,errorThrown) {
          self.updateAlerts(  $.parseJSON(jqXHR.responseText).errors);
        };

        // 401 response handler
        self.Unauthorized = function(jqXHR,textStatus,errorThrown) {
          self.incorrectPassword(true);
          self.updateAlerts(  $.parseJSON(jqXHR.responseText).errors);
        };

        // 500 response handler
        self.InternalServerError = function(jqXHR, textStatus,errorThrown) {
            self.updateAlerts(  $.parseJSON(jqXHR.responseText).errors);
        };

        //503 response handler
        self.ServiceUnavailable = function(jqXHR, textStatus,errorThrown) {
            self.updateAlerts(  $.parseJSON(jqXHR.responseText).errors);
        };



        // API call: update password
        self.updatePassword = function() {
            self.loading(true);
            $.ajax("/ajax/account/password",
                {data: ko.toJSON({ password: self.currentPassword, newPassword: self.newPassword })
                , type: "put"
                , contentType: "application/json"
                , statusCode: {
                    202: self.Accepted,
                    400: self.BadRequest,
                    401: self.Unauthorized,
                    500: self.InternalServerError,
                    503: self.ServiceUnavailable
                  }
                }
            ).always(function() { self.loading(false);  })
        };



        // dismiss the modal without change
        self.cancel = function() {
            self.modal.close();
        }
    };


    return  (ChangePasswordModal);
});