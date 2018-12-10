%%% trees.mp, version 0.3, 5 June 2002
%%% Copyright (c) 2002 by David Chiang.



%%% version 0.3: added variables for positioning trees
%%% version 0.2: align nodes by baseline instead of bottom edge

def drawedge (suffix a, b) =
  draw a.s--b.n;
enddef;

def drawroof (suffix a, b) =
  draw a.s--b.nw--b.ne--cycle;
enddef;

newinternal levelsep, treesep;
levelsep := 30pt;
treesep := 6pt;

vardef cleart_(suffix $) =
  _n_ := str $;
  generic_redeclare(numeric) _n.children, _n.edge;
  generic_redeclare(numeric) _n.tc, _n.tsw, _n.ts, _n.tse, _n.te, _n.tne, _n.tn, _n.tnw, _n.tw;
  
enddef;

vardef _treeit@# (expr t, c) =
  boxit@#(t);

  _n_ := str @#;
  generic_declare(string) _n.children, _n.edge;
  generic_declare(pair) _n.tc, _n.tsw, _n.ts, _n.tse, _n.te, _n.tne, _n.tn, _n.tnw, _n.tw;

  0 = xpart (@#tnw-@#tsw) = ypart(@#tse-@#tsw);
  0 = xpart(@#tne-@#tse) = ypart(@#tne-@#tnw);
  @#tw = .5[@#tnw,@#tsw];
  @#ts = .5[@#tsw,@#tse];
  @#te = .5[@#tne,@#tse];
  @#tn = .5[@#tne,@#tnw];
  @#tc = .5[@#tnw,@#tse];
  
  @#edge := "drawedge";
  
  @#children := c;

  expandafter def expandafter clearboxes expandafter =
    clearboxes cleart_(@#);
  enddef;
enddef;

vardef treeit@# (text t, c) =
  save children;
  string children;
  
  children := "";
  forsuffixes $ = c:
    if children = "":
      children := str $;
    else:
      children := children & "," & str $;
    fi;
  endfor;

  _treeit@#(t,children);
enddef;

vardef leafit@# (text t) =
  treeit@# (t)();
enddef;

_n_anon := 0;

vardef tree@# (text t, c) =
  save children;
  string children;
  
  children := "";
  for child = c:
    if children = "":
      children := if picture child: leaf(child) else: child fi;
    else:
      children := children & "," & if picture child: leaf(child) else: child fi;
    fi;
  endfor;

  if (str @#) = "":
    _n_anon := _n_anon + 1;
    _treeit._anon[_n_anon-1](t,children);
    str _anon[_n_anon-1]
  else:
    _treeit@#(t,children);
    str @#
  fi
enddef;

vardef leaf@# (text t) =
  tree@#(t)()
enddef;

def _sizetree (suffix parent) =
  begingroup
    save first, fw, fe, fs, cw, ce;
    boolean first;

    fixsize(parent);
    
    first := true;

    if (parent.children <> ""):
      forsuffixes child = scantokens parent.children:

	if unknown (ypart(parent.off)-ypart(child.off)):
	  ypart(parent.off)-ypart(child.off) = levelsep;
	fi;
	
	_sizetree(child);
	if (first):
	  fw := xpart(child.tw);
	  cw := xpart(child.w);
	  fs := ypart(child.ts);
	  first := false;
	else:
	  if unknown (xpart(child.tw)-fe):
	    xpart(child.tw)-fe = treesep;
	  fi;
	  fs := min(fs, ypart(child.ts));
	fi;
	fe := xpart(child.te);
	ce := xpart(child.e);
      endfor;

      % Center parent over outermost children
      if unknown (xpart(parent.c)-0.5(ce+cw)):
	xpart(parent.c) = 0.5(ce+cw);
      fi;

      xpart(parent.te) = max(fe, xpart(parent.e));
      xpart(parent.tw) = min(fw, xpart(parent.w));
      ypart(parent.tn) = ypart(parent.n);
      ypart(parent.ts) = fs;
    else:
      parent.tnw = parent.nw;
      parent.tse = parent.se;
    fi;
  endgroup
enddef;

def _drawtree (suffix parent) =
  drawunboxed(parent);
  if (parent.children <> ""):
    forsuffixes child = scantokens parent.children:
      scantokens child.edge(parent, child);
      _drawtree(child);
    endfor;
  fi;
enddef;

def drawtrees (text t) =
  forsuffixes s = t:
    _sizetree(s);
    _drawtree(s);
  endfor;
enddef;

def def_nonterminal(suffix $)(expr pic) =
  vardef $.@#(text children) =
    tree@# (pic) (children)
  enddef;
enddef;

def def_terminal(suffix $)(expr pic) =
  vardef $.@# =
    leaf@# (pic)
  enddef;
enddef;
