#!/bin/bash

src=../Tube-design-straight.pnm

x1=2725
x2=9257
x3=15721
x4=22256


y1=5012
y2=12100
y3=19115
y4=26196

file=xxx_00_00.png
echo $file
pnmcut -left $x1 -right $x2 -top $y1 -bottom $y2 <$src | pnmtopng -compression 0 > $file

file=xxx_01_00.png
echo $file
pnmcut -left $x2 -right $x3 -top $y1 -bottom $y2 <$src | pnmtopng -compression 0 > $file

file=xxx_02_00.png
echo $file
pnmcut -left $x3 -right $x4 -top $y1 -bottom $y2 <$src | pnmtopng -compression 0 > $file

file=xxx_00_01.png
echo $file
pnmcut -left $x1 -right $x2 -top $y2 -bottom $y3 <$src | pnmtopng -compression 0 > $file

file=xxx_01_01.png
echo $file
pnmcut -left $x2 -right $x3 -top $y2 -bottom $y3 <$src | pnmtopng -compression 0 > $file

file=xxx_02_01.png
echo $file
pnmcut -left $x3 -right $x4 -top $y2 -bottom $y3 <$src | pnmtopng -compression 0 > $file

file=xxx_00_02.png
echo $file
pnmcut -left $x1 -right $x2 -top $y3 -bottom $y4 <$src | pnmtopng -compression 0 > $file

file=xxx_01_02.png
echo $file
pnmcut -left $x2 -right $x3 -top $y3 -bottom $y4 <$src | pnmtopng -compression 0 > $file

file=xxx_02_02.png
echo $file
pnmcut -left $x3 -right $x4 -top $y3 -bottom $y4 <$src | pnmtopng -compression 0 > $file

 
