module d_latch(Q, NQ, D, EN);
  output Q, NQ;
  input D, EN;

  wire D1, D2;

  nor nor1(D1, D, EN);
  nor nor2(D2, D1, EN);
  nor nor3(Q, NQ, D1);
  nor nor4(NQ, Q, D2);

endmodule
