all: scrypt vcrypt feistel	

scrypt:	
	javac scrypt.java	
	echo '#!/bin/bash' > scrypt	
	echo 'java scrypt "$$@"' >> scrypt	
	chmod u+x scrypt	
vcrypt:	
	javac vcrypt.java	
	echo '#!/bin/bash' > vcrypt	
	echo 'java vcrypt "$$@"' >> vcrypt	
	chmod u+x vcrypt	
feistel:	
	javac feistel.java	
	echo '#!/bin/bash' > feistel	
	echo 'java feistel "$$@"' >> feistel	
	chmod u+x feistel	
clean:	
	rm -f *.class scrypt vcrypt feistel	