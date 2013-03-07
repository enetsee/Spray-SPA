define(['services/logger','durandal/app', 'viewmodels/account', 'viewmodels/changePasswordModal'], function (logger,app,Account,ChangePasswordModal) {




    var AccountModal = function() {

        ko.validation.configure({
            insertMessages: false,
            decorateElement: true,
            errorElementClass: 'error'
        });

        // Data
        var self = this;

        self.email = ko.observable('').extend({ email: true });
        self.updatingEmail = ko.observable(false);

        self.accountName = ko.observable('').extend({minLength : 2});
        self.updatingName = ko.observable(false);

        $.getJSON("/ajax/account",
            function(data) {
                var act = new Account(data);
                self.email(act.email);
                self.accountName(act.name);
            }
        );


        self.activate = function() {
           logger.log('AccountModal activated', null, 'accountModal');
           return true;
        };




        self.updateName = function() {
            self.updatingName(true);
            $.ajax("/ajax/account/name",
                {data: ko.toJSON(self.accountName)
                 , type: "put"
                 , contentType: "application/json"
                 , statusCode: {
                    202: function(data,textStatus,jqXHR) { /*Do something useful*/ },
                    400: function(jqXHR,textStatus,errorThrown) { /*Do something useful  */ },
                    401: function(jqXHR,textStatus,errorThrown) { /*Re-authenticate user */ },
                    500: function(jqXHR,textStatus,errorThrown) { /*Do something useful  */ },
                    503: function(jqXHR,textStatus,errorThrown) { /*Do something useful  */ }
                    }
                 }
            ).always(function() { self.updatingName(false);  });
        }


        self.updateEmail = function() {
            self.updatingEmail(true);
            $.ajax("/ajax/account/email",
                {data: ko.toJSON(self.email)
                 , type: "put"
                 , contentType: "application/json"
                 , statusCode: {
                    202: function(data,textStatus,jqXHR) { /*Do something useful*/ },
                    400: function(jqXHR,textStatus,errorThrown) { /*Do something useful  */ },
                    401: function(jqXHR,textStatus,errorThrown) { /*Re-authenticate user */ },
                    500: function(jqXHR,textStatus,errorThrown) { /*Do something useful  */ },
                    503: function(jqXHR,textStatus,errorThrown) { /*Do something useful  */ }
                    }
                 }
            ).always(function() { self.updatingEmail(false);  });
        }


        self.showPasswordDialog = function() {
            app.showModal(new ChangePasswordModal());
        }

        self.ok = function() {
            self.modal.close();
        }
    };
    return  (AccountModal);
});