#!/bin/bash

src=../Tube-design.pnm

l_00=977
t_00=3115
r_00=9504
b_00=12296

file=xxx_00.png
echo $file
pnmcut -left $l_00 -right $r_00 -top $t_00 -bottom $b_00 <$src | pnmtopng -compression 0 > $file
