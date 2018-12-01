verbatimtex
etex

input expressg;

beginfig(1);
z3=(0,0);
drawovalbox(3,40,17)(btex \tt start etex);
z1c=z3c-(0,40);
drawdiamondbox(1,45,25)(btex \tt x > 5 etex);
z2c=z1c-(0,40);
drawLEVENT(2,55,15)(btex \tt x=read(); etex);
z4c=z2c-(0,40);
drawENT(4,40,15)(btex \tt x=5; etex);
z5=z3c-(0,160);
drawovalbox(5,40,17)(btex \tt stop etex);

drawnormalCA(3bm,1tm);
drawnormalCA(2bm,4tm);
drawnormalCA(4bm,5tm);
endfig;
end;