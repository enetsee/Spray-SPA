define(['services/logger','durandal/app','viewmodels/chatmessage'], function (logger,app,ChatMessage) {

    function vm() {
        var self = this;
        self.title = 'Chat';

        self.messages = ko.observableArray([]);
        self.newChatMessage = ko.observable('');

        self.sendChatMessage = function() {
            $.ajax("/ajax/chat",
                {data: ko.toJSON({"message": self.newChatMessage})
                 , type: "post"
                 , contentType: "application/json"
                 , statusCode: {
                    202: function(data,textStatus,jqXHR) { /*Do something useful*/ },
                    400: function(jqXHR,textStatus,errorThrown) { /*Do something useful  */ },
                    401: function(jqXHR,textStatus,errorThrown) { /*Re-authenticate user */ },
                    500: function(jqXHR,textStatus,errorThrown) { /*Do something useful  */ },
                    503: function(jqXHR,textStatus,errorThrown) { /*Do something useful  */ }
                    }
                 }
             ).always(function() { self.newChatMessage(''); })
        };

        // event source
        self.makeEventSource = function() {
           var s = new EventSource("/streaming/chat");
           s.addEventListener("message", function(e) {
           var parsed = JSON.parse(e.data);
           var msg = new ChatMessage(parsed);

            self.messages.push(msg);
           }, false);
           return s;
        };

        self.source = self.makeEventSource();



        // Durandal vm lifecycle
        self.activate = function() {
            logger.log('Chat activated', null, 'chat');
            return true;
        };

        self.deactivate = function() {
            self.source.close();
        };


    };

    return (new vm());



});