#!/bin/bash

src=../Tube-design.pnm

x0=977
x1=8904
x2=9504
x3=15304
x4=16048
x5=24148
y0=3115
y1=11800
y2=12296
y3=18856
y4=19368
y5=28092

file=xxx_00.png
echo $file
pnmcut -left $x0 -right $x2 -top $y0 -bottom $y2 <$src | pnmtopng -compression 0 > $file

file=xxx_10.png
echo $file
pnmcut -left $x1 -right $x4 -top $y0 -bottom $y2 <$src | pnmtopng -compression 0 > $file

file=xxx_20.png
echo $file
pnmcut -left $x3 -right $x5 -top $y0 -bottom $y2 <$src | pnmtopng -compression 0 > $file

file=xxx_01.png
echo $file
pnmcut -left $x0 -right $x2 -top $y1 -bottom $y4 <$src | pnmtopng -compression 0 > $file

file=xxx_11.png
echo $file
pnmcut -left $x1 -right $x4 -top $y1 -bottom $y4 <$src | pnmtopng -compression 0 > $file

file=xxx_21.png
echo $file
pnmcut -left $x3 -right $x5 -top $y1 -bottom $y4 <$src | pnmtopng -compression 0 > $file

file=xxx_02.png
echo $file
pnmcut -left $x0 -right $x2 -top $y3 -bottom $y5 <$src | pnmtopng -compression 0 > $file

file=xxx_12.png
echo $file
pnmcut -left $x1 -right $x4 -top $y3 -bottom $y5 <$src | pnmtopng -compression 0 > $file

file=xxx_22.png
echo $file
pnmcut -left $x3 -right $x5 -top $y3 -bottom $y5 <$src | pnmtopng -compression 0 > $file
