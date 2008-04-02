close all;
clear all;

BT1 = load('UC3MLogBT1.dat');
BT2 = load('UC3MLogBT2.dat');
BT3 = load('UC3MLogBT3.dat');
BT4 = load('UC3MLogBT4.dat');
BT5 = load('UC3MLogBT5.dat');
BT6 = load('UC3MLogBT6.dat');
BT7 = load('UC3MLogBT7.dat');
BT8 = load('UC3MLogBT8.dat');
BT9 = load('UC3MLogBT9.dat');
BT10 = load('UC3MLogBT10.dat');
BT1b = load('UC3MLogBT1b.dat');
BT2b = load('UC3MLogBT2b.dat');
BT3b = load('UC3MLogBT3b.dat');
BT4b = load('UC3MLogBT4b.dat');
BT5b = load('UC3MLogBT5b.dat');
BT6b = load('UC3MLogBT6b.dat');
BT7b = load('UC3MLogBT7b.dat');
BT8b = load('UC3MLogBT8b.dat');
BT9b = load('UC3MLogBT9b.dat');
BT10b = load('UC3MLogBT10b.dat');
le = 214;
le2 = 198;

t = 1:1:10;
x = t'*ones(1,le);
x = x';
x = x(:);

x2 = t'*ones(1,le2);
x2 = x2';
x2 = x2(:);


y = [BT1(1:le);BT2(1:le);BT3(1:le);BT4(1:le);BT5(1:le);BT6(1:le);BT7(1:le);BT8(1:le);BT9(1:le);BT10(1:le)]';
m1 = mean(y,1);
v1 = var(y,0,1);
y = y(:);

yb = [BT1b(1:le2);BT2b(1:le2);BT3b(1:le2);BT4b(1:le2);BT5b(1:le2);BT6b(1:le2);BT7b(1:le2);BT8b(1:le2);BT9b(1:le2);BT10b(1:le2)]';
m2 = mean(yb,1);
v2 = var(yb,0,1)
yb = yb(:);

figure();
plot(x,y,'xr');
hold on;
plot(x2,yb,'*g');
plot(1:1:10,m1,'r');
plot(1:1:10,m2,'g');

t = abs(tinv(0.035,213));
icu1 = m1 + t*v1/sqrt(214);
icl1 = m1 - t*v1/sqrt(214);

icu2 = m2 + t*v2/sqrt(214);
icl2 = m2 - t*v2/sqrt(214);

disp('Confidence Intervals BW=16Kbps');

[icu1;m1;icl1]'

disp('Confidence Intervals BW=128Kbps');

[icu2;m2;icl2]'
