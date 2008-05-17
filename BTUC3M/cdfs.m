clear all;
close all;


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%One thousand nodes simulations with no node's location %
%restrictions.                                          %
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%x_sn_sbw = load('CDF_sn_sbw.dat');
%x_sn_hbw = load('CDF_sn_hbw.dat');
%x_sn_sbw2 = load('CDF_sn_sbw_120508_1243.dat');
%x_sn_hbw2 = load('CDF_sn_hbw_130508_0057.dat');
%x_sn_hbw3 = load('CDF_sn_hbw_130508_1725.dat');
%x_sn_hbw4 = load('CDF_sn_hbw_140508_0010.dat');
%x_sn_hbw5 = load('CDF_sn_hbw_140508_1330.dat');
%x_nn_sbw = load('CDF_nn_sbw.dat');
%x_nn_hbw = load('CDF_nn_hbw.dat');
%x_nn_hbw2 = load('CDF_nn_hbw_1405081709.dat');
%x_nn_hbw3 = load('CDF_nn_hbw_140508_2156.dat');
%x_nn_hbw4 = load('CDF_nn_hbw_150508_0001.dat');
%x_nn_hbw5 = load('CDF_nn_hbw_150508_0153.dat');
%x_nn_hbw6 = load('CDF_nn_hbw_150508_0310.dat');
%x_nn_hbw7 = load('CDF_nn_hbw_150508_1200.dat');


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%One hundred nodes simulations, nodes limited to  %
%one hundrer first nodes enlisted                 %
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

x_sn1 = load('CDF_sn_hbw_1_80.dat');

%x_sn_hbw = load('CDF_sn_hbw_1_100.dat');
%x_sn_hbw2 = load('CDF_sn_hbw_2_100.dat');
%x_sn_hbw3 = load('CDF_sn_hbw_3_100.dat');
%x_sn_hbw4 = load('CDF_sn_hbw_4_100.dat');

%x_sn2_hbw = load('CDF_sn2_hbw_1_100.dat');
%x_sn2_hbw2 = load('CDF_sn2_hbw_2_100.dat');
%x_sn2_hbw3 = load('CDF_sn2_hbw_3_100.dat');
%x_sn2_hbw4 = load('CDF_sn2_hbw_4_100.dat');

%x_sn3_hbw = load('CDF_sn3_hbw_1_100.dat');
%x_sn3_hbw2 = load('CDF_sn3_hbw_2_100.dat');
%x_sn3_hbw3 = load('CDF_sn3_hbw_3_100.dat');

x_sn4_hbw = load('CDF_sn4_hbw_1_100.dat');
x_sn4_hbw2 = load('CDF_sn4_hbw_2_100.dat');
x_sn4_hbw3 = load('CDF_sn4_hbw_3_100.dat');
x_sn4_hbw4 = load('CDF_sn4_hbw_4_100.dat');
x_sn4_hbw5 = load('CDF_sn4_hbw_5_100.dat');

x_sn5_hbw = load('CDF_sn5_hbw_1_100.dat');
x_sn5_hbw2 = load('CDF_sn5_hbw_2_100.dat');
x_sn5_hbw3 = load('CDF_sn5_hbw_3_100.dat');
x_sn5_hbw4 = load('CDF_sn5_hbw_4_100.dat');
x_sn5_hbw5 = load('CDF_sn5_hbw_5_100.dat');
x_sn5_hbw6 = load('CDF_sn5_hbw_6_100.dat');

x_sn6_hbw1 = load('CDF_sn6_hbw_1_80.dat');
x_sn6_hbw2 = load('CDF_sn6_hbw_2_80.dat');
x_sn6_hbw3 = load('CDF_sn6_hbw_3_80.dat');
x_sn6_hbw4 = load('CDF_sn6_hbw_4_80.dat');
x_sn6_hbw5 = load('CDF_sn6_hbw_5_80.dat');
x_sn6_hbw6 = load('CDF_sn6_hbw_6_80.dat');

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

%h = cdfplot(x_sn_hbw);
%set(h,'Color',[0 0 0]);
%set(h,'LineWidth',2);
%h = cdfplot(x_sn_hbw2);
%set(h,'Color',[0 0 0]);
%set(h,'LineWidth',2);
%h = cdfplot(x_sn_hbw3);
%set(h,'Color',[0 0 0]);
%set(h,'LineWidth',2);

%h = cdfplot(x_sn2_hbw);
%set(h,'Color',[1 0 0]);
%set(h,'LineWidth',2);
%h = cdfplot(x_sn2_hbw2);
%set(h,'Color',[1 0 0]);
%set(h,'LineWidth',2);
%h = cdfplot(x_sn2_hbw3);
%set(h,'Color',[1 0 0]);
%set(h,'LineWidth',2);
%h = cdfplot(x_sn2_hbw4);
%set(h,'Color',[1 0 0]);
%set(h,'LineWidth',2);

%h = cdfplot(x_sn3_hbw);
%set(h,'Color',[0 1 0]);
%set(h,'LineWidth',2);
%h = cdfplot(x_sn3_hbw2);
%set(h,'Color',[0 1 0]);
%set(h,'LineWidth',2);
%h = cdfplot(x_sn3_hbw3);
%set(h,'Color',[0 1 0]);
%set(h,'LineWidth',2);

%h = cdfplot(x_sn4_hbw);
%set(h,'Color',[0 1 1]);
%set(h,'LineWidth',2);
%h = cdfplot(x_sn4_hbw2);
%set(h,'Color',[0 1 1]);
%set(h,'LineWidth',2);
%h = cdfplot(x_sn4_hbw3);
%set(h,'Color',[0 1 1]);
%set(h,'LineWidth',2);
%h = cdfplot(x_sn4_hbw4);
%set(h,'Color',[0 1 1]);
%set(h,'LineWidth',2);
%h = cdfplot(x_sn4_hbw5);
%set(h,'Color',[0 1 1]);
%set(h,'LineWidth',2);

%h = cdfplot(x_sn5_hbw);
%set(h,'Color',[1 0 0]);
%set(h,'LineWidth',2);
%h = cdfplot(x_sn5_hbw2);
%set(h,'Color',[1 0 0]);
%set(h,'LineWidth',2);
pause;
%h = cdfplot(x_sn5_hbw3);
%set(h,'Color',[0 0 0]);
%set(h,'LineWidth',1);
h = cdfplot(x_sn5_hbw4);
set(h,'Color',[0 0 0]);
set(h,'LineWidth',1);
h = cdfplot(x_sn5_hbw5);
set(h,'Color',[0 0 0]);
set(h,'LineWidth',1);
h = cdfplot(x_sn5_hbw6);
set(h,'Color',[0 0 0]);
set(h,'LineWidth',1);

pause;
h = cdfplot(x_sn6_hbw1);
set(h,'Color',[1 0 0]);
set(h,'LineWidth',2);
h = cdfplot(x_sn6_hbw2);
set(h,'Color',[1 0 0]);
set(h,'LineWidth',2);
h = cdfplot(x_sn6_hbw3);
set(h,'Color',[1 0 0]);
set(h,'LineWidth',2);
h = cdfplot(x_sn6_hbw4);
set(h,'Color',[1 0 0]);
set(h,'LineWidth',2);
h = cdfplot(x_sn6_hbw5);
set(h,'Color',[1 0 0]);
set(h,'LineWidth',2);
h = cdfplot(x_sn6_hbw6);
set(h,'Color',[1 0 0]);
set(h,'LineWidth',2);