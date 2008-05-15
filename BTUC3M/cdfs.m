clear all;
close all;


x_sn_sbw = load('CDF_sn_sbw.dat');
x_sn_hbw = load('CDF_sn_hbw.dat');
x_sn_sbw2 = load('CDF_sn_sbw_120508_1243.dat');
x_sn_hbw2 = load('CDF_sn_hbw_130508_0057.dat');
x_sn_hbw3 = load('CDF_sn_hbw_130508_1725.dat');
x_sn_hbw4 = load('CDF_sn_hbw_140508_0010.dat');
x_sn_hbw5 = load('CDF_sn_hbw_140508_1330.dat');
x_nn_sbw = load('CDF_nn_sbw.dat');
%x_nn_hbw = load('CDF_nn_hbw.dat');
%x_nn_hbw2 = load('CDF_nn_hbw_1405081709.dat');
%x_nn_hbw3 = load('CDF_nn_hbw_140508_2156.dat');
%x_nn_hbw4 = load('CDF_nn_hbw_150508_0001.dat');
%x_nn_hbw5 = load('CDF_nn_hbw_150508_0153.dat');
%x_nn_hbw6 = load('CDF_nn_hbw_150508_0310.dat');
%x_nn_hbw7 = load('CDF_nn_hbw_150508_1200.dat');

x_nn_hbw = load('CDF_nn_hbw_1_100.dat');
x_nn_hbw2 = load('CDF_nn_hbw_2_100.dat');
x_nn_hbw3 = load('CDF_nn_hbw_3_100.dat');
x_nn_hbw4 = load('CDF_nn_hbw_4_100.dat');
x_nn_hbw5 = load('CDF_nn_hbw_5_100.dat');
x_nn_hbw6 = load('CDF_nn_hbw_6_100.dat');
x_nn_hbw7 = load('CDF_nn_hbw_7_100.dat');
x_nn_hbw8 = load('CDF_nn_hbw_8_100.dat');
x_nn_hbw9 = load('CDF_nn_hbw_9_100.dat');
x_nn_hbw10 = load('CDF_nn_hbw_10_100.dat');

x_sn_hbw = load('CDF_sn_hbw_1_100.dat');
x_sn_hbw2 = load('CDF_sn_hbw_2_100.dat');
x_sn_hbw3 = load('CDF_sn_hbw_3_100.dat');
x_sn_hbw4 = load('CDF_sn_hbw_4_100.dat');

figure;
title('Normal Nodes');
cdfplot(x_nn_hbw);
hold on;
pause;
cdfplot(x_nn_hbw2);
pause;
cdfplot(x_nn_hbw3);
pause;
cdfplot(x_nn_hbw4);
pause;
cdfplot(x_nn_hbw5);
pause;
cdfplot(x_nn_hbw6);
pause;
cdfplot(x_nn_hbw7);
pause;
cdfplot(x_nn_hbw8);
pause;
cdfplot(x_nn_hbw9);
pause;
cdfplot(x_nn_hbw10);
pause;

figure;
title('Normal Nodes');
cdfplot(x_sn_hbw);
hold on;
pause;
cdfplot(x_sn_hbw2);
pause;
cdfplot(x_sn_hbw3);
pause;
cdfplot(x_sn_hbw4);
pause;
%cdfplot(x_sn_hbw2);
%pause;
%cdfplot(x_sn_hbw3);
%pause;
%cdfplot(x_sn_hbw5);
%pause;
%cdfplot(x_nn_sbw);
%pause;
%cdfplot(x_sn_sbw);
%pause;
%cdfplot(x_sn_sbw2);
%pause;

