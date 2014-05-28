nfcreader
=========

Code for CardIO NFC Reader

Implements a stand-alone NFC reader for ACR122 from Advanced Card Systems. It debounces multiple reads, tosses spurious read messages and does an http GET to a client app running a lite webserver (like Node.JS).

The NFC interface code was borrowed from another open source repo, but I do not remember where :(.

And I apologize for the salty error messages. Feel free to change if you borrow this code.
