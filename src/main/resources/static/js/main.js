$(document).ready(function() {
    // Submit form to set personalities and start conversation
    $('#startConversationForm').submit(function(event) {
        console.log("Form submitted"); // Debugging statement
        event.preventDefault();
        clearMessageBoard();
        var ai1Personality = $('#ai1Personality').val();
        var ai2Personality = $('#ai2Personality').val();
        var topic = $('#topic').val();
        setPersonalitiesAndStartConversation(ai1Personality, ai2Personality, topic);
    });

    var conversation = ''; // storing the convo so far.
    var latestMessage = ''; // Variable to store the latest message received
    var conversationLengthLimit = 1000; // Assuming conversation length limit is 1000 characters


    // Function to set personalities and start conversation
    function setPersonalitiesAndStartConversation(ai1Personality, ai2Personality, topic) {
        // Prepare the system messages to be sent with the personalities
        var message1 = "You're the 1st actor in a chatroom with someone else. Limit yourself to at most 240 characters per response. Continue whatever conversation given to you as if you've been part of it all along, and try not to repeat yourself and your opening words too often. And your personality and/or belief is: " + ai1Personality;
        var message2 = "You're the 2nd actor in a chatroom with someone else. Limit yourself to at most 240 characters per response. Continue whatever conversation given to you as if you've been part of it all along, and try not to repeat yourself and your opening words too often. And your personality and/or belief is: " + ai2Personality;

        // Prepare the data to be sent in the POST request
        var data1 = {
            message: message1
        };

        var data2 = {
            message: message2
        };

        // Send POST request to app1 (hosted on port 8080) to set personality for actor 1
        $.ajax({
            type: 'POST',
            url: 'http://localhost:8080/chat/system-message',
            contentType: 'text/plain',
            data: message1,
            success: function() {
                console.log('Personality for actor 1 set successfully.');
            },
            error: function() {
                alert('Error setting personality for actor 1.');
            }
        });

        // Send POST request to app2 (hosted on port 8081) to set personality for actor 2
        $.ajax({
            type: 'POST',
            url: 'http://localhost:8081/chat/system-message',
            contentType: 'text/plain',
            data: message2,
            success: function() {
                console.log('Personality for actor 2 set successfully.');
                // Once both personalities are set, start the conversation
                startConversation(topic);
            },
            error: function() {
                alert('Error setting personality for actor 2.');
            }
        });
    }

    // Function to start conversation
    function startConversation(topic) {
        console.log('Starting conversation with topic:', topic);
        initiateConversation('AI 1', topic); // Start the conversation from AI 1
    }

    // Function to initiate conversation from a specific AI
    function initiateConversation(sender, message) {
        // Determine the URL based on the sender
        var url = sender === 'AI 1' ? 'http://localhost:8080/chat/send-message' : 'http://localhost:8081/chat/send-message';

        // Send the message to the appropriate app
        $.ajax({
            type: 'POST',
            url: url,
            contentType: 'application/json',
            data: JSON.stringify({ message: message }),
            success: function(response) {
                console.log('Received response from', sender + ':', response);
                displayMessage(sender, response);
                conversation += response; // Add the response to the conversation history
                latestMessage = response; // Update the latest message received

                // Determine the next sender
                var nextSender = sender === 'AI 1' ? 'AI 2' : 'AI 1';

                // Continue the conversation if it's not complete
                if (!conversationIsComplete(conversation)) {
                    initiateConversation(nextSender, response);
                }
            },
            error: function() {
                alert('Error initiating conversation from ' + sender + '.');
            }
        });
    }

    // Function to check if the conversation is complete
    function conversationIsComplete(conversation) {
        return conversation.length >= conversationLengthLimit; // Assuming conversationLengthLimit is defined elsewhere
    }

    // Attach click event listener to the "Generate New Response" button
    $('#generateNewResponseBtn').click(function() {
        generateResponse(latestMessage); // Generate a new response using the latest message received
    });

    // Function to generate response for the message received from app2
    function generateResponse(message) {
        // Send the message to ChatGPT for response
        $.ajax({
            type: 'POST',
            url: '/chat/send-message', // Assuming this endpoint sends message to ChatGPT
            contentType: 'application/json',
            data: JSON.stringify({ message: message }),
            success: function(response) {
                displayNewResponse(response); // Display the new response
            },
            error: function() {
                alert('Error generating response.');
            }
        });
    }

    // Function to display the new response
    function displayNewResponse(response) {
        $('#messageList').append('<li><strong>Generated Response:</strong> ' + response + '</li>');
    }

    // Function to display message in chat
    function displayMessage(sender, message) {
        $('#messageList').append('<li><strong>' + sender + ':</strong> ' + message + '</li>');
    }

    // Function to clear message board
            function clearMessageBoard() {
        $('#messageList').empty();
        conversation = '';
    }
});
