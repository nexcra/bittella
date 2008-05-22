clear all;
close all;




%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%80 nodes simulations, nodes limited to  %
%80 first nodes enlisted                 %
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

x_nn_hbw = load('CDF_nn_hbw_1_80.dat');
x_nn_hbw2 = load('CDF_nn_hbw_2_80.dat');
x_nn_hbw3 = load('CDF_nn_hbw_3_80.dat');
x_nn_hbw4 = load('CDF_nn_hbw_4_80.dat');
x_nn_hbw5 = load('CDF_nn_hbw_5_80.dat');
x_nn_hbw6 = load('CDF_nn_hbw_6_80.dat');
x_nn_hbw7 = load('CDF_nn_hbw_7_80.dat');
x_nn_hbw8 = load('CDF_nn_hbw_8_80.dat');
x_nn_hbw9 = load('CDF_nn_hbw_9_80.dat');
x_nn_hbw10 = load('CDF_nn_hbw_10_80.dat');
x_nn_hbw11 = load('CDF_nn_hbw_11_80.dat');

x_sn1 = load('CDF_sn1_hbw_1_80.dat');
%x_sn2 = load('CDF_sn1_hbw_2_80.dat');
%x_sn3 = load('CDF_sn1_hbw_3_80.dat');

figure(1);
title('Normal Nodes');
hold on;
cdfplot(x_nn_hbw);
cdfplot(x_nn_hbw2);
cdfplot(x_nn_hbw3);
cdfplot(x_nn_hbw4);
cdfplot(x_nn_hbw5);
cdfplot(x_nn_hbw6);
cdfplot(x_nn_hbw7);
cdfplot(x_nn_hbw8);
cdfplot(x_nn_hbw9);
cdfplot(x_nn_hbw10);
pause;
cdfplot(x_nn_hbw11);
pause;
h = cdfplot(x_sn1);
set(h,'Color',[1 0 0]);
set(h,'LineWidth',2);
%h = cdfplot(x_sn2);
%set(h,'Color',[1 0 0]);
%set(h,'LineWidth',2);
%pause;
%h = cdfplot(x_sn3);
%set(h,'Color',[1 1 0]);
%set(h,'LineWidth',2);
