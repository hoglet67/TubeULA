module sr_latch(NQ, Q, S, R);

output Q, NQ;
input S, R;

// nor #5 nor1(Q, R, NQ);
// nor #5 nor2(NQ, S, Q);

reg state;

initial
   begin
     state = 1;
   end
 
always @(S or R)
   begin
     if (S == 1)
         state = 1;
     else if (R == 1)
         state = 0;
   end
    
assign #5 Q = state;
assign #5 NQ = ~state;

endmodule
