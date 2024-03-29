//#include <arpa/inet.h>
//#include <netinet/in.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <unistd.h> //close()
#include <stdlib.h> //malloc() free()
#include <string.h>  //memset()
#include <netdb.h>  //getaddrinfo()
#include <stdio.h> //gets()
#define BUFLEN 512
#define PORT "9930"
#define SRV_IP "127.0.0.1"
#define SRV_IP6 "2001::1234:1"

struct dato{
 char nombre[30];
 char apellido[25];
 int edad;
};


 void diep(char *s)
  {
   perror(s);
   exit(1);
  }


  int main(void)
  {
    struct addrinfo hints;
    struct addrinfo *result, *rp;
   // struct sockaddr_in si_other;
    int s,cd, i;
    char buf[BUFLEN];

/*    if ((s=socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP))==-1)
      diep("socket");

    memset((char *) &si_other, 0, sizeof(si_other));
    si_other.sin_family = AF_INET;
    si_other.sin_port = htons(PORT);
    if (inet_aton(SRV_IP, &si_other.sin_addr)==0) {
      fprintf(stderr, "inet_aton() failed\n");
      exit(1);
    }  */

   /* Obtain address(es) matching host/port */

   memset(&hints, 0, sizeof(struct addrinfo));
    hints.ai_family = AF_UNSPEC;    /* Allow IPv4 or IPv6 */
    hints.ai_socktype = SOCK_DGRAM; /* Datagram socket */
    hints.ai_flags = 0;
    hints.ai_protocol = 0;          /* Any protocol */

   s = getaddrinfo(SRV_IP6, PORT, &hints, &result);
    if (s != 0) {
        fprintf(stderr, "getaddrinfo: %s\n", gai_strerror(s));
        exit(EXIT_FAILURE);
    }

   /* getaddrinfo() returns a list of address structures.
       Try each address until we successfully connect(2).
       If socket(2) (or connect(2)) fails, we (close the socket
       and) try the next address. */

   for (rp = result; rp != NULL; rp = rp->ai_next) {
        cd = socket(rp->ai_family, rp->ai_socktype,rp->ai_protocol);
        if (cd == -1)
            continue;
	break;
      /* if (connect(cd, rp->ai_addr, rp->ai_addrlen) != -1)
            break;                  // Success //

       close(cd);  */
    }

   if (rp == NULL) {               /* No address succeeded */
        fprintf(stderr, "Could no hay direcciones disponibles\n");
        exit(EXIT_FAILURE);
    }

   freeaddrinfo(result);           /* No longer needed */

     struct dato *o1;      o1 = (struct dato *)malloc(sizeof (struct dato));
     printf("Escribe el nombre:");
     gets(o1->nombre);
     printf("Escribe el apellido:");
     gets(o1->apellido);
     printf("Escribe la edad:");
     int ed;
     scanf("%d",&ed);
     fflush(stdin);
     o1->edad=htonl(ed);
  



      printf("Sending packet\n");
      //sprintf(buf, "This is packet %d\n", i);
      if (sendto(cd, (const char*)o1, sizeof(struct dato), 0, (struct sockaddr *)rp->ai_addr, rp->ai_addrlen)==-1)
        diep("sendto()");
   
     free(o1);
    close(s);
    return 0;
  }
