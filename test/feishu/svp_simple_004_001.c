volatile int g1;
volatile int g2;

void svp_simple_004_001_main(){
    g2=3; 
    g1=2;
    g2=3;
}

void svp_simple_004_001_isr_1() {
    if(g1==2)
    {
        int r1=g2; 
    }
}