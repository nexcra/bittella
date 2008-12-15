#!/bin/bash

if [ ${#*} -ne 1 ]; then
    echo "usage: $0 <network-specs.sh> [> network.graph]";
    exit 1;
fi

source $1

function gen_vertices() {
    local vid
    local cid
    for ((vid=0;vid<$STUBS;vid++)) do
	echo -e "\t\t<vertex int_idx=\"$vid\" role=\"gateway\" />"
    done
    echo

    for ((cid=0;cid<$CLIENTS;cid++,vid++)) do
	echo -e "\t\t<vertex int_idx=\"$vid\" role=\"virtnode\" int_vn=\"$cid\"/>"
    done
}

function gen_edges() {
    local eid
    local src
    local dst
    echo -e "\t\t<!-- client-stub connections -->"
    for ((eid=0,src=0,dst=$STUBS;src<$STUBS;src++,dst++,eid++)) do
	echo -e "\t\t<edge int_src=\"$src\" int_dst=\"$dst\" int_idx=\"$eid\" specs=\"client-stub\" />"
        let eid=eid+1;
	echo -e "\t\t<edge int_src=\"$dst\" int_dst=\"$src\" int_idx=\"$eid\" specs=\"client-stub\" />"
    done
    echo
    
    echo -e "\t\t<!-- stub-stub connections -->"
    for ((src=0;src<$STUBS;src++)) do
	for ((dst=0;dst<$STUBS;dst++)) do
	    if [ $src -ne $dst ]; then
		echo -e "\t\t<edge int_src=\"$src\" dbl_len=\"1\" int_dst=\"$dst\" int_idx=\"$eid\" specs=\"stub-stub\" />"
    		let eid=eid+1;
	    fi
	done
    done
}

function gen_specs() {
    echo -e "\t\t<client-stub dbl_plr=\"0\" dbl_kbps=\"$BANDWIDTH\" int_delayms=\"$CLIENT_STUB_DELAY\" int_qlen=\"10\" />"
    echo -e "\t\t<stub-stub dbl_plr=\"0\" dbl_kbps=\"$BANDWIDTH\" int_delayms=\"$STUB_STUB_DELAY\" int_qlen=\"10\" />"
}

echo "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>"
echo -e "<topology>"
echo -e "\t<vertices>"
gen_vertices
echo -e "\t</vertices>"
echo
echo -e "\t<edges>"
gen_edges
echo -e "\t</edges>"
echo
echo -e "\t<specs>"
gen_specs
echo -e "\t</specs>"
echo -e "</topology>"