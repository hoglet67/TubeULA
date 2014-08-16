module d_latch(q, nq, d, en);
  output q, nq;
  input d, en;

  wire d1, d2;

  nor nor1(d1, d, en);
  nor nor2(d2, d1, en);
  nor nor3(q, nq, d1);
  nor nor4(nq, q, d2);

endmodule
