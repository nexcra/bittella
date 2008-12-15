/*Convert king latency matrix to modelnet graph format
 */

#include<stdio.h>
#include<stdlib.h>
#include<string.h>

//const int ss_delayms = 0;
const int ss_qlen = 1000;
const double ss_plr = 0;
const double ss_kbps = 1048576;

const int cs_delayms = 0;
const int cs_qlen = 1000;
const double cs_plr = 0;
const double cs_kbps = 1048576;

int** delays;
int* badrows;
char* errormsg;
int NOOFNODES=0;

void initdelays();
void getdelays(const char* filepath);
void scandelays();
void writexml();
void printdelays();
void cleanup();
void usage();

int main(int argc, char * argv[]) {

	errormsg = (char*) malloc(sizeof(char*)*100);

	if (argc < 3) {
		usage();
		exit(1);
	}

	NOOFNODES = atoi(argv[2]);

	initdelays();
	getdelays(argv[1]);
	//printdelays();

	scandelays();

	writexml();

	cleanup();

	return 0;
}

void getdelays(const char* filepath) {

	int MAX_LINE_SIZE = NOOFNODES*10;
	//warn("Starting...\n");
	FILE *infile;
	infile = fopen(filepath, "rt");

	if (infile == NULL) {

		err(1, "Can't open %s", filepath);
		exit(1);
	}

	char* line = (char*) malloc(sizeof(char)*MAX_LINE_SIZE);
	line[0] = '\0';
	char* newline = NULL;
	int source = 0;

	while (!feof(infile) && source<NOOFNODES) {

		fgets(line, MAX_LINE_SIZE, infile);
		newline = line;
		//warn("\n%d - %s\n\n", source+1, newline);

		char* word = malloc(sizeof(char)*20);
		int dest = 0;
		int readbytes = 0;
		while (sscanf(newline, "%s ", word) != EOF && dest<NOOFNODES) {

			delays[source][dest] = atoi(word);
			if (delays[source][dest] == -1)
				delays[source][dest] = 0;

			readbytes = strlen(word)+1;
			newline = newline + readbytes;
			//warn("length of newline: %d", strlen(newline));
			++dest;

		}

		if (dest==0)
			--source;

		++source;
		line[0] = '\0';
	}

	fclose(infile);
	//fclose(outfile);

	//sprintf(errormsg, "Total lines read = %d\n", source);
	//perror(errormsg);	

}

void scandelays() {

	int i, j;
	badrows = (int*) malloc(sizeof(int)*NOOFNODES);

	for (i=0; i<NOOFNODES; ++i) {
		badrows[i]=0;
	}

	for (i=0; i<NOOFNODES; ++i) {
		for (j=0; j<NOOFNODES; ++j) {
			if (delays[i][j] == -1) {
				badrows[i] = 1;
				sprintf(errormsg, "Row: %d, Col: %d is bad", i, j);
				perror(errormsg);
				//break;
			}
		}
	}
}

void writexml() {

	printf("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");

	printf("<topology>\n");

	printf("\t<vertices>\n");

	int i;
	for (i=0; i<NOOFNODES; ++i) {
		printf("\t\t<vertex int_idx=\"%d\" role=\"gateway\"/>\n", i);
	}

	int VIRTNODES=0;
	VIRTNODES = 2*NOOFNODES;
	printf("\n");
	for (; i<VIRTNODES; ++i) {
		printf(
				"\t\t<vertex int_idx=\"%d\" role=\"virtnode\" int_vn=\"%d\" />\n",
				i, i-NOOFNODES);
	}

	printf("\t</vertices>\n\n");

	printf("\t<edges>\n");
	printf("\t\t<!-- client-stub connections -->\n");
	int edgeindex = 0;
	for (i=0; i<NOOFNODES; ++i) {
		printf(
				"\t\t<edge int_src=\"%d\" int_dst=\"%d\" int_idx=\"%d\" specs=\"client-stub\"/>\n",
				i, i+NOOFNODES, edgeindex++);
		printf(
				"\t\t<edge int_src=\"%d\" int_dst=\"%d\" int_idx=\"%d\" specs=\"client-stub\"/>\n",
				i+NOOFNODES, i, edgeindex++);
	}

	printf("\n\t\t<!-- stub-stub connections -->\n");

	int j;

	for (i=0; i<NOOFNODES; ++i) {
		for (j=0; j<NOOFNODES; ++j) {
			printf(
					"\t\t<edge int_src=\"%d\" dbl_len=\"1\" int_dst=\"%d\" int_idx=\"%d\" int_delayms=\"%d\" specs=\"stub-stub\" />\n",
					i, j, edgeindex++, delays[i][j]);
		}

	}

	printf("\t</edges>\n\n");

	printf("\t<specs>\n");
	printf(
			"\t\t<client-stub dbl_plr=\"%.0f\" dbl_kbps=\"%.0f\" int_delayms=\"%d\" int_qlen=\"%d\" />\n",
			cs_plr, cs_kbps, cs_delayms, cs_qlen);
	printf(
			"\t\t<stub-stub dbl_plr=\"%.0f\" dbl_kbps=\"%.0f\" int_delayms=\"0\" int_qlen=\"%d\" />\n",
			ss_plr, ss_kbps, ss_qlen);
	printf("\t</specs>\n");

	printf("</topology>\n");
}

void initdelays() {

	int i;
	delays = (int**) malloc(sizeof(int*)*NOOFNODES);

	for (i=0; i<NOOFNODES; ++i) {
		delays[i] = (int*) malloc(sizeof(int)*NOOFNODES);
	}
}

void printdelays() {

	int i, j;

	for (i=0; i<NOOFNODES; ++i) {
		for (j=0; j<NOOFNODES; ++j) {

			printf("Delay from %d - %d is %d\n", i+1, j+1, delays[i][j]);
		}
		printf("\n");
	}

}

void cleanup() {

	free(badrows);
	free(errormsg);

	int i;
	for (i=0; i<NOOFNODES; ++i) {
		free(delays[i]);
	}
	free(delays);
}

void usage() {
	perror("usage:king2xml <latencies-file> <noofnodes>\n");
}
