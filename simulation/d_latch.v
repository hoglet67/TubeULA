module d_latch(Q, NQ, D, EN);
  output Q, NQ;
  input D, EN;

  wire D1, D2;

  nor #5 nor1(D1, D, EN);
  nor #5 nor2(D2, D1, EN);
  nor #5 nor3(Q, NQ, D1);
  nor #5 nor4(NQ, Q, D2);

endmodule
