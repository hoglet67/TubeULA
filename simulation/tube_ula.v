‘timescale 1 ns / 100 ps

module tube_ula_testbench

// Inputs

reg DACK;
reg HA<2:0>;
reg HCS;
reg HDIN<7:0>;
reg HO2;
reg HRST;
reg HRW;
reg PA<2:0>;
reg PCS;
reg PDIN<7:0>;
reg PNRDS;
reg PNWDS;

// Outputs

wire DRQ;
wire HDOUT<7:0>;
wire HDOE;
wire HIRQ;
wire PDOUT<7:0>
wire PDOE;
wire PIRQ;
wire PNMI;
wire PRST;

// Instantaiate gate level design

tube_ula U1 (

     // Inputs

    .DACK(DACK),
    .HA2(HA<2>),
    .HA1(HA<1>),
    .HA0(HA<0>),
    .HCS(HCS),
    .HD7IN(HDIN<7>),
    .HD6IN(HDIN<6>),
    .HD5IN(HDIN<5>),
    .HD4IN(HDIN<4>),
    .HD3IN(HDIN<3>),
    .HD2IN(HDIN<2>),
    .HD1IN(HDIN<1>),
    .HD0IN(HDIN<0>),
    .HO2(HO2),
    .HRST(HRST),
    .HRW(HRW),
    .PA2(PA<2>),
    .PA1(PA<1>),
    .PA0(PA<0>),
    .PCS(PCS),
    .PD7IN(PDIN<7>),
    .PD6IN(PDIN<6>),
    .PD5IN(PDIN<5>),
    .PD4IN(PDIN<4>),
    .PD3IN(PDIN<3>),
    .PD2IN(PDIN<2>),
    .PD1IN(PDIN<1>),
    .PD0IN(PDIN<0>),
    .PNRDS(PNRDS),
    .PNWDS(PNWDS),

    // Outputs

    .DRQ(DRQ),
    .HD7OUT(HDOUT<7>),
    .HD6OUT(HDOUT<6>),
    .HD5OUT(HDOUT<5>),
    .HD4OUT(HDOUT<4>),
    .HD3OUT(HDOUT<3>),
    .HD2OUT(HDOUT<2>),
    .HD1OUT(HDOUT<1>),
    .HD0OUT(HDOUT<0>),
    .HDOE(HDOE),
    .HIRQ(HIRQ),
    .PD7OUT(PDOUT<7>),
    .PD6OUT(PDOUT<6>),
    .PD5OUT(PDOUT<5>),
    .PD4OUT(PDOUT<4>),
    .PD3OUT(PDOUT<3>),
    .PD2OUT(PDOUT<2>),
    .PD1OUT(PDOUT<1>),
    .PD0OUT(PDOUT<0>),
    .PDOE(PDOE),
    .PIRQ(PIRQ),
    .PNMI(PNMI),
    .PRST(PRST)
);


integer i;

initial
    begin
        DACK = 0;
        HA<2:0> = 0;
        HCS = 1;
        HDIN<7:0> = 0;
        HO2 = 0;
        HRST = 1;
        HRW = 1;
        PA<2:0> = 0;
        PCS = 1;
        PDIN<7:0> = 0;
        PNRDS = 1;
        PNWDS = 1;

        // Synchronously assert HRST for 10 clocks
        @ (negedge HO2);
        HRST = 0;
        repeat (10)
        begin
            @ (negedge HO2);
        end
        HRST = 1;

        // Allow system to settle for 10 clocks
        repeat (10)
        begin
            @ (negedge HO2);
        end

        // Read all of the Host Registers
        for (i = 0; i <= 7; i = i + 1)
        begin
            HA = i;
            @ (negedge HO2);
            HCS = 0;
            @ (negedge HO2);
            HCS = 1;
            @ (negedge HO2);
            @ (negedge HO2);
            @ (negedge HO2);
        end

        // Read all of the Parasite Registers
        for (i = 0; i <= 7; i = i + 1)
        begin
            PA = i;
            @ (negedge HO2);
            PNRDS = 0;
            @ (negedge HO2);
            PNRDS = 1;
            @ (negedge HO2);
            @ (negedge HO2);
            @ (negedge HO2);
        end

        $finish;

    end

endmodule;
