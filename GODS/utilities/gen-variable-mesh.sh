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
				echo -e "\t\t<edge int_src=\"$src\" dbl_len=\"1\" int_dst=\"$dst\" int_idx=\"$eid\" specs=\"stub$src-stub$dst\" />"
    			let eid=eid+1;
	    	fi
		done
    done
}
#################################################################
# The following function generates random numbers				#
# The number of arguments can be in between 0-3. When specified #
# ARG1 : Minimum value for generated random numbers				#
# ARG2 : Maximum value of the range of random numbers		 	#
# ARG3 : The random numbers should be divisible by this value 	#
################################################################# 
random=0;
function gen_random() {
	
	# Default values assigned, in case parameters not passed to function.
	local min=${1:-0}
   	local max=${2:-32767}
   	local divisibleBy=${3:-1}
   	
 	random=$(((RANDOM%(max-min+divisibleBy))/divisibleBy*divisibleBy+min));
}

function gen_specs() {
	local src
    local dst
    local bandwidth
    local delay
    
	echo -e "\t\t<!-- client-stub specs -->"
    echo -e "\t\t<client-stub dbl_plr=\"0\" dbl_kbps=\"$BANDWIDTH\" int_delayms=\"$CLIENT_STUB_DELAY\" int_qlen=\"10\" />"
    
    echo -e "\t\t<!-- stub-stub specs -->"
	for ((src=0;src<$STUBS;src++)) do
		for ((dst=0;dst<$STUBS;dst++)) do
	    	if [ $src -ne $dst ]; then
	    		gen_random $MIN_STUB_STUB_DELAY $MAX_STUB_STUB_DELAY 10 #random delays divisible by 10
	    		delay=$random
	    		gen_random $MIN_BANDWIDTH $MAX_BANDWIDTH 2 #random bandwidth divisible by 2
	    		bandwidth=$random
    			echo -e "\t\t<stub$src-stub$dst dbl_plr=\"0\" dbl_kbps=\"$bandwidth\" int_delayms=\"$delay\" int_qlen=\"10\" />"
			fi
		done
    done
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