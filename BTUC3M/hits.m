

clear all;
close all;
aux = load('HitStats_110408_1845.dat');

hit = aux(1:6,:);
pass = aux(7:12,:);

a = find(pass == 0);
pass(a)=1;

p = (hit./pass)*100;


h = bar3(p);
for i = 1:length(h)
    zdata = ones(6*size(p,1),4);
    k = 1;
    for j = 0:6:(6*size(p,1)-6)
        zdata(j+1:j+6,:) = p(k,i);
        k = k+1;
    end
    set(h(i),'Cdata',zdata)
end
colormap copper
colorbar

shading interp
for i = 1:length(h)
    zdata = get(h(i),'Zdata');
    set(h(i),'Cdata',zdata)
    set(h,'EdgeColor','k')
end

view([0 90]);