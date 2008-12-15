#!/bin/bash

edge_idx=0
nodes=$1

#specs
ss_dbl_plr=0
ss_dbl_kbps=1048576
ss_int_qlen=1000
cs_dbl_plr=0
cs_int_delayms=0
cs_dbl_kbps=1048576
cs_int_qlen=1000

function make_edge() 
{
    local s=$1 
    local d=$2
    local l=$3
    echo -e "\t\t<edge int_src=\"$s\" dbl_len=\"1\" int_dst=\"$d\" int_idx=\"$edge_idx\" int_delayms=\"$l\" specs=\"stub-stub\" />"
    let edge_idx=edge_idx+1;
}

if [ $# -ne 1 ]; then
    echo "Usage: $0 <nodes_cnt>"
    exit;
fi

echo "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>"
echo "<topology>"
#generate vertices
echo -e "\t<vertices>"
for ((i=0;i<$nodes;i++)); do
    echo -e "\t\t<vertex int_idx=\"$i\" role=\"gateway\"/>";
done
for ((i=0;i<$nodes;i++)); do
    echo -e "\t\t<vertex int_idx=\"$(($i+$nodes))\" role=\"virtnode\" int_vn=\"$i\"/>";
done
echo -e "\t</vertices>"
echo

#generate edges
echo -e "\t<edges>"
echo -e "\t\t<!-- client-stub connections -->"
for ((i=0;i<$nodes;i++)); do
    echo -e "\t\t<edge int_src=\"$i\" int_dst=\"$(($i+$nodes))\" int_idx=\"$edge_idx\" specs=\"client-stub\" />";
    let edge_idx=edge_idx+1;
    echo -e "\t\t<edge int_src=\"$(($i+$nodes))\" int_dst=\"$i\" int_idx=\"$edge_idx\" specs=\"client-stub\" />";
    let edge_idx=edge_idx+1;
done

echo
echo -e "\t\t<!-- stub-stub connections -->"
IFS=,
while read s d l ; do
    make_edge $(($s-1)) $(($d-1)) $l
done
echo -e "\t</edges>"

#generate specs
echo -e "\t<specs>"
echo -e "\t\t<client-stub dbl_plr=\"$cs_dbl_plr\" dbl_kbps=\"$cs_dbl_kbps\" int_delayms=\"$cs_int_delayms\" int_qlen=\"$cs_int_qlen\" />"
echo -e "\t\t<stub-stub dbl_plr=\"$ss_dbl_plr\" dbl_kbps=\"$ss_dbl_kbps\" int_delayms=\"0\" int_qlen=\"$ss_int_qlen\" />"
echo -e "\t</specs>"
echo -e "</topology>"
