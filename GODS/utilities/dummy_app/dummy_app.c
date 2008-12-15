/*
 * Dummy application that logs the time of being launched/stopped/killed.
 * author: Cosmin Arad
 */
#include <stdio.h>
#include <stdlib.h>
#include <signal.h>
#include <errno.h> 
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/time.h>
#include <time.h>
#include <unistd.h>
#include <fcntl.h>

int stopsig, killsig, ret, logfd;
long int nodeid;
struct timeval tv;
char logstr[100];

void usage(char *name)
{
    printf("Usage: %s <nodeid> <logfilename> <stopsigno> <killsigno>\n", name);
}

void log_action(int action)
{
    int cnt;
    ret = gettimeofday(&tv, NULL);
    if (ret) perror("gettimeofday");


    switch (action) {
	case 0:
	cnt = sprintf(logstr, "%12ld.%06ld\t%12ld J\n",
	    (long int)tv.tv_sec, (long int)tv.tv_usec, nodeid);
	write(logfd, logstr, cnt);
	break;

	case 1:
	cnt = sprintf(logstr, "%12ld.%06ld\t%12ld L\n",
	    (long int)tv.tv_sec, (long int)tv.tv_usec, nodeid);
	write(logfd, logstr, cnt);
	break;

	case 2:
	cnt = sprintf(logstr, "%12ld.%06ld\t%12ld F\n",
	    (long int)tv.tv_sec, (long int)tv.tv_usec, nodeid);
	write(logfd, logstr, cnt);
	break;
    }
    printf("Wrote %d bytes:%s", cnt, logstr);

    sync();    
}

void signal_handler(int signo, siginfo_t *info, void *ptr)
{
    printf("Caught signal %d.\n", signo);

    if (signo == stopsig) {
	log_action(1); // LEAVE
    } else if (signo == killsig) {
	log_action(2); // FAIL
    }
}

int main(int argc, char **argv)
{
    char *logfile;
    
    sigset_t blocked_signals;
    int sa_flags = SA_SIGINFO;
    struct sigaction sa;
    
    ret = sigemptyset(&blocked_signals);
    if (ret) perror("sigemptyset");
    
    sa.sa_sigaction = signal_handler;
    sa.sa_mask = blocked_signals;
    sa.sa_flags = sa_flags;
    
    if (argc != 5) {
	usage(argv[0]);
	exit(1);
    }
    
    sscanf(argv[1], "%ld", &nodeid);
    logfile = argv[2];
    sscanf(argv[3], "%d", &stopsig);
    sscanf(argv[4], "%d", &killsig);
    
    logfd = open(logfile, O_WRONLY|O_APPEND|O_SYNC|O_CREAT, S_IRWXU|S_IRWXG|S_IRWXO);
    if (logfd == -1) perror("open");
    
    ret = sigaction(stopsig, &sa, NULL);
    if (ret) perror("sigaction");
    
    ret = sigaction(killsig, &sa, NULL);
    if (ret) perror("sigaction");
    
    log_action(0); // JOIN
    
    pid_t pid = getpid();
    
    printf("My PID is %d\n", (int) pid);
    printf("My Node ID is %ld\n", nodeid);
    printf("Logging to %s\n", logfile);
    printf("Leaving on signal %d\n", stopsig);
    printf("Dying on signal %d\n", killsig);
    
    sigsuspend(&blocked_signals);
    
    exit(0);
}
