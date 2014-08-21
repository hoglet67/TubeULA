`timescale 1 ns / 100 ps

module tube_ula_testbench();

// Inputs

reg DACK;
reg [2:0] HA;
reg HCS;
reg [7:0] HDIN;
wire [7:0] HDININV;
wire HO2;

reg HO2early;
reg HRST;
reg HRW;
reg [2:0] PA;
reg PCS;
reg [7:0] PDIN;
reg PNRDS;
reg PNWDS;

wire PNRDSgated;
wire PNWDSgated;

// Outputs

wire DRQ;
wire [7:0] HDOUT;
wire HDOE;
wire HIRQ;
wire [7:0] PDOUT;
wire PDOE;
wire PIRQ;
wire PNMI;
wire PRST;

assign HDININV = ~HDIN;

   task host_write;
      input [2:0] addr;
      input [7:0] data;
      begin
        @ (negedge HO2);
        HA = addr;
        HDIN = data;
        HRW = 0;
        HCS = 0;
        @ (negedge HO2);        
        HCS = 1;
        HA = 3'bXXX;
        HDIN = 8'bXXXXXXXX;
        HRW = 1;
      end         
    endtask
    
    task host_read;
      input [2:0] addr;
      begin
        @ (negedge HO2);
        HA = addr;
        HRW = 1;
        HCS = 0;
        @ (negedge HO2);        
        HCS = 1;
        HA = 3'bXXX;
        HRW = 1;
      end         
    endtask    

   task para_write;
      input [2:0] addr;
      input [7:0] data;
      begin
        @ (negedge HO2);
        PA = addr;
        PDIN = data;
        PNWDS = 0;
        PCS = 0;
        @ (negedge HO2);        
        PCS = 1;
        PA = 3'bXXX;
        PDIN = 8'bXXXXXXXX;
        PNWDS = 1;
      end         
    endtask

    task para_read;
      input [2:0] addr;
      begin
        @ (negedge HO2);
        PA = addr;
        PNRDS = 0;
        PCS = 0;
        @ (negedge HO2);        
        PCS = 1;
        PA = 3'bXXX;
        PNRDS = 1;
      end         
    endtask
    
    task delay;
        input integer n;
        begin
            repeat (n)
            begin
                @ (negedge HO2);
            end
        end
    endtask
    
    task test_host_to_para_fifo;
        input [2:0] status;
        input [2:0] fifo;
        input integer num;
        begin
            host_read(status);
            para_read(status);
            for (i = 0; i < num; i = i + 1)
            begin
                host_write(fifo, 170 + i); // write to host
            end
            host_read(status);
            para_read(status);
            for (i = 0; i < num; i = i + 1)
            begin
                para_read(fifo);         // read from para
            end
            host_read(status);
            para_read(status);        
        end
    endtask
    
    task test_para_to_host_fifo;
        input [2:0] status;
        input [2:0] fifo;
        input integer num;
        begin
            para_read(status);
            host_read(status);
            for (i = 0; i < num; i = i + 1)
            begin
                para_write(fifo, 170 + i); // write to para
            end
            para_read(status);
            host_read(status);
            for (i = 0; i < num; i = i + 1)
            begin
                host_read(fifo);     // read from host
            end
            para_read(status);
            host_read(status);        
        end
    endtask
    
// Instantaiate gate level design

tube_ula U1 (

     // Inputs

    .DACK(DACK),
    .HA2(HA[2]),
    .HA1(HA[1]),
    .HA0(HA[0]),
    .HCS(HCS),
    .HD7IN(HDININV[7]),
    .HD6IN(HDININV[6]),
    .HD5IN(HDININV[5]),
    .HD4IN(HDININV[4]),
    .HD3IN(HDININV[3]),
    .HD2IN(HDININV[2]),
    .HD1IN(HDININV[1]),
    .HD0IN(HDININV[0]),
    .HO2(HO2early),
    .HRST(HRST),
    .HRW(HRW),
    .PA2(PA[2]),
    .PA1(PA[1]),
    .PA0(PA[0]),
    .PCS(PCS),
    .PD7IN(PDIN[7]),
    .PD6IN(PDIN[6]),
    .PD5IN(PDIN[5]),
    .PD4IN(PDIN[4]),
    .PD3IN(PDIN[3]),
    .PD2IN(PDIN[2]),
    .PD1IN(PDIN[1]),
    .PD0IN(PDIN[0]),
    .PNRDS(PNRDSgated),
    .PNWDS(PNWDSgated),

    // Outputs

    .DRQ(DRQ),
    .HD7OUT(HDOUT[7]),
    .HD6OUT(HDOUT[6]),
    .HD5OUT(HDOUT[5]),
    .HD4OUT(HDOUT[4]),
    .HD3OUT(HDOUT[3]),
    .HD2OUT(HDOUT[2]),
    .HD1OUT(HDOUT[1]),
    .HD0OUT(HDOUT[0]),
    .HDOE(HDOE),
    .HIRQ(HIRQ),
    .PD7OUT(PDOUT[7]),
    .PD6OUT(PDOUT[6]),
    .PD5OUT(PDOUT[5]),
    .PD4OUT(PDOUT[4]),
    .PD3OUT(PDOUT[3]),
    .PD2OUT(PDOUT[2]),
    .PD1OUT(PDOUT[1]),
    .PD0OUT(PDOUT[0]),
    .PDOE(PDOE),
    .PIRQ(PIRQ),
    .PNMI(PNMI),
    .PRST(PRST)
);


integer i;

    
initial
    begin
        DACK = 1;
        HA = 0;
        HCS = 1;
        HDIN = 8'bXXXXXXXX;
        HO2early = 0;
        HRST = 1;
        HRW = 1;
        PA = 0;
        PCS = 1;
        PDIN = 0;
        PNRDS = 1;
        PNWDS = 1;

        // Synchronously assert HRST for 50 clocks
        
        @ (negedge HO2);
        HRST = 0;
        repeat (50)
        begin
            @ (negedge HO2);
        end
        HRST = 1;

        delay(10);
        
        // Take PRST high, low, high
        host_write(3'b000, 8'b00100000);
        host_write(3'b000, 8'b10100000);
        host_write(3'b000, 8'b00100000);
        host_read(3'b000);
        delay(10);

        // Get rid of X's from address pointers
        delay(10);

        // De-Assert soft reset (up until this point it will be X)
        host_write(3'b000, 8'b01000000);
        delay(10);
        
        // Assert soft reset for atleast 24 clocks to flush the 24 byte FIFO
        host_write(3'b000, 8'b11000000);
        delay(50);
        
//        para_write(3'b101, 8'b11111111);
//        host_read(3'b101);
//        host_write(3'b101, 8'b11111111);
//        para_read(3'b101);
//        delay(10);
//        
        // De-Assert  soft reset
        host_write(3'b000, 8'b01000000);
        delay(10);

//        host_read(3'b101);
//        para_read(3'b101);
//        delay(10);

        // Disable all interrupts
        host_write(3'b000, 8'b00011111);

        // Set two byte mode for register 3
        host_write(3'b000, 8'b10010000);
        host_read(3'b000);


        // Read the junk byte out of register 3
        host_read(3'b101);



        delay(10);
        
        
        // Test Host To Para
        
        // Test Register 1
        test_host_to_para_fifo(3'b000, 3'b001, 1);
        delay(10);
        // Test Register 2
        test_host_to_para_fifo(3'b010, 3'b011, 1);
        delay(10);        
        // Test Register 3
        test_host_to_para_fifo(3'b100, 3'b101, 2);
        delay(10);
        // Test Register 4
        test_host_to_para_fifo(3'b110, 3'b111, 1);
        delay(10);
        
        // Test Para to Host
        
        // Test Register 1
        test_para_to_host_fifo(3'b000, 3'b001, 24);
        delay(10);
        // Test Register 2
        test_para_to_host_fifo(3'b010, 3'b011, 1);
        delay(10);                
        // Test Register 3
        test_para_to_host_fifo(3'b100, 3'b101, 2);
        delay(10);
        // Test Register 4
        test_para_to_host_fifo(3'b110, 3'b111, 1);
        delay(10);

    end
    
    always
    begin
        #500 HO2early = ~HO2early;
    end

    assign #250 HO2 = HO2early;
    
    assign PNRDSgated = PNRDS | ~HO2early;
    assign PNWDSgated = PNWDS | ~HO2early;

endmodule
