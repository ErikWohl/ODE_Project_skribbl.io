# Command structure
command|values<br>

command ... 3 Zeichen langer Befehl<br>
| ... separator, wird nicht mitgeschickt<br>
values ... <br> werden je nach Befehl anders abgearbeitet
## Serverside Abarbeitung
•	Message, Drawing, Clear: Werden einfach an die anderen Clients via Multicast verschickt (keine Serverseitige verarbeitung nötig.<br>

1   Ein client führt den --startgame; Befehl aus.<br> 
2	Start game request --> Server schickt einen broadcast an alle.<br>
3	Start game ACK/NACK --> Clients müssen acknowledgen oder ablehnen bei Fehler.<br>
4	Guesser/Drawer request --> Danach wird am Server der Drawer und die Guesser bestimmt und losgeschickt.<br>
5   Guesser warten auf den Runden beginn oder einen Error. == ENDE FÜR GUESSER<br>
6	Drawer ACK --> Drawer bekommt im Request Wörter und such sich eines davon aus und schickt es an den Server.<br>
7	Round start request --> Server hat Antwort vom Drawer bekommen und will die Runde starten.<br>
8	Round start ACK/NACK --> Alle clients stimmen für den Rundenbeginn ab (Status gesetzt == ACK, bei error = NACK).<br>
9	Round started --> Server hat alle ACKs bekommen. Startet Runde. Sendet GUESSERN ein leeres Wort ("_") und dem Drawer das eigentliche Wort<br>

Jederzeit während den Requests soll es möglich sein einen ERR zu schicken um wieder von neu zu beginnen.
Bei Step 6 gibt es einen 20 Sekunden Timeouot, wenn bis dahin nichts gesendet wird, wird es einen error geben.

•	STARTROUND<br>

## Serverside Requests
•	Guesser Request: GSR|<br>
•	Drawer Request: DRW|word1;word2;word3<br>
•	Round start Request: RSR|<br>
•	Round started : RST|word<br>

•	Error : ERR|<br>


## Clientside Requests
### Controller Requests
•	Message: MSG|text<br>
•	Drawing: DRW|x1;y1;x2;y2;size;color<br>
•	Clear: CLR|<br>

### Gamelogic Requests
•	Start game reqest: SGR|<br>

•	Start game acknowledgement: SGA|<br>
•	Start game not acknowledgement: SGN|<br>

•	Drawer acknowledgement: DWA|<br>

•	Round acknowledgement: RSA|<br>
•	Round not acknowledgement: RSN|<br>