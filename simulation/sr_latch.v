module sr_latch(NQ, Q, S, R);

output Q, NQ;
input S, R;

nor nor1(Q, S, NQ);
nor nor2(NQ, R, Q);

endmodule
