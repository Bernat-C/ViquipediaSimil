## Objectiu General
Aquesta pràctica consisteix a crear una aplicació amb Scala amb l'objectiu de poder comparar documents entre sí, per saber com de similars són. Això ho farem mitjançant Vector Space Models, que seran vectors que contindran cada paraula d'un text i el pes associat a aquesta, que serà un valor entre 0.0 i 1.0. Aquests vectors s'utilitzaran per computar la similitud entre dos fitxers, mitjançant la similitud de cosinus.

## Parts del treball
Aquest treball consta de dues parts. En la primera compararem dos textos de llibres assignant un pes a cada paraula dins de cada text, és a dir, que emprarem els vectors de term frequencies (tf) (frequència de paraules), i on tots els càlculs es realitzaran de manera seqüencial. En la segona utilitzarem arxius xml de la Vikipèdia, i la similitud es podrà calcular amb molts documents a la vegada, assignant pesos a les paraules en funció de com de significatives són dins el corpus (conjunt de pàgines), de manera que farem servir els vectors tf\_idf (term frequency i inverse document frequency) i efectuarem operacions en paral·lel mitjançant l'algorisme MapReduce. A més, en aquesta segona part no només tindrem un sol text, sinó que dividirem els fitxers en títol, contingut i referències. D'aquesta manera, a banda de detectar pàgines similars podrem dur a terme altres accions com determinar quines són les pàgines més rellevants (les que són referenciades més vegades) o calcular la mitjana d'imatges o referències en les pàgines.

## Algorisme MapReduce
L'algorisme de MapReduce mencionat anteriorment és una tècnica que normalment s'utilitza quan cal tractar amb conjunts de dades molt grans, i en casos en què la feina es pot paral·lelitzar, ja que aquest és un dels seus avantatges més importants. Una vegada s'ha definit l'estructura general del MapReduce cal crear una funció de mapping i una funció de reducing per cada tasca que vulguem dur a terme.
La capacitat de paral·lelització d'aquesta tècnica rau en el fet que podem tenir múltiples mappers, que faran servir la funció de mapping, i reducers, que usaran la funció de reducing, treballant a la vegada. 
Generalment, cada mapper rebrà una part de l'entrada inicial i la tractarà mitjançant la funció de mapping. Tots els resultats obtinguts s'aniran acumulant en un mapa el qual, una vegada tots els mappers hagin acabat, es dividirà en parts per a ser tractat per cada reducer, mitjançant la funció de reducing. Quan l'últim reducer hagi acabat la seva feina, l'algorisme de MapReduce ens donarà el resultat obtingut.

## Estructura de Fitxers
Al directori arrel tenim la carpeta src/main/scala, on podem trobar dues subcarpetes amb tot el codi que hem fet. 

A la subcarpeta main hi tenim els fitxers JocsDeProves.scala, que conté totes les funcions que podem executar per provar la segona part de l'aplicació ràpidament, el fitxer Main.scala, que és el nostre punt d'entrada a l'aplicació i és el que hauríem d'executar per fer servir l'aplicació, MappersAndReducers.scala, que és on tenim les definicions de totes les funcions de mapping i de reducing, i SimilitudEntreDocuments.scala, que és el fitxer amb què vam començar fent la part 1 de la pràctica, i conté tot el codi relacionat amb la similitud entre llibres.

A la subcarpeta mapreduce hi tenim els fitxers MapReduceActors.scala, amb tot el codi que se'ns va donar en un principi del MapReduce, lleugerament modificat per permetre passar el nombre de mappers i de reducers per paràmetre, també tenim el fitxer ProcessListStrings.scala, que també és part del codi inicial que se'ns va donar, altra vegada modificat una mica per afegir una funció que ens permet, a partir del nom d'un fitxer, retornar el seu títol, contingut i llista de referències en una tupla, i finalment el fitxer ViquipediaParsing.scala, que si bé no l'hem modificat en cap moment, l'utilitzem en la funció que hem mencionat anteriorment.

Altra vegada al directori arrel, tenim la carpeta primeraPartPractica, que conté els diferents fitxers de llibres i el fitxer de stopwords en anglès que se'ns va proporcionar a la primera part. 

També al directori arrel hi tenim la carpeta viqui_files, que conté tots els fitxers xml de les pàgines de la Viquipèdia que se'ns va proporcionar a la segona part de la pràctica.

Finalment, fora de cap subcarpeta, directament al directori arrel hi tenim dos fitxers de stopwords en català, stopwordscat.txt i stopwordscatalanet.txt. En el nostre cas hem fet servir el segon fitxer.

## Com utilitzar l'aplicació
Per utilitzar l'aplicació només cal baixar-la des d'aquest mateix repositori, obrir-la amb un IDE com per exemple IntelliJ IDEA (o compilar-la si així es prefereix) i executar-la. Un cop l'aplicació estigui en funcionament, només cal llegir les instruccions que es mostren per pantalla i escollir l'opció desitjada. Simplement cal entrar per teclat el nombre o lletra de l'opció que es vulgui triar i prémer ENTER. Un cop s'hagi fet això, si calen accions addicionals per part de l'usuari, és a dir, proporcionar paràmetres a l'aplicació, així es mostrarà per pantalla, i només caldrà repetir el procés anterior per introduir les dades necessàries.

Cal tenir en compte que algunes operacions, sobretot les que tractin amb la similitud entre un nombre de pàgines elevat, poden tardar força estona.
