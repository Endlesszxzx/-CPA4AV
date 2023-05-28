volatile int g1;
volatile int g2;

void svp_simple_003_001_main(){
    int r1=g2;
    if(r1==2)
    { 
        g2=5;
    }
    else
    {
        g2=9;
    }
}

void svp_simple_003_001_isr_1() {
    g2=3;
}