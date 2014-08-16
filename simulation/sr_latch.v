module sr_latch(nq, q, s, r);

output q, nq;
input s, r;

nor n1(q, s, nq);
nor n2(nq, r, q);

endmodule
