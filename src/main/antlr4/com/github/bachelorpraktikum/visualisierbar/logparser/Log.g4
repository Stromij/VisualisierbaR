grammar Log;

start: (element_line | error_char)+;

element_line: elements NEWLINE;
elements:         node
                | edge
                | elem
                | view
                | logicalGroup
                | also
                | extra
                | train
                | mv_init
                | mv_leaves
                | mv_speed
                | mv_start
                | mv_term
                | ch
                | msg
                | data;

node: 'NODE' SEP node_name SEP coord (SEP node_abs_name)?;

edge: 'EDGE' SEP edge_name SEP node_name SEP node_name SEP INT (SEP edge_abs_name)?;

elem: 'ELEM' SEP elem_name SEP node_name SEP STATE (SEP elem_abs_name)?;

view: 'TOWARDS' SEP elem_name SEP edge_name (SEP speed)?;

logicalGroup: 'GROUP' SEP kind SEP log_name SEP sw_name SEP (elem_name SEP)*;

also: 'ALSO' SEP elem_name SEP log_name;

extra: 'EXTRA' SEP elem_name SEP value SEP name SEP bool;

train: 'ZUG' SEP train_name SEP train_readable_name SEP INT;

mv_init: MV_IND SEP 'INIT' SEP train_name SEP time SEP edge_name;

mv_speed: MV_IND SEP 'SPEED' SEP train_name SEP time SEP distance (SEP speed)?;

mv_start: MV_IND SEP 'REACHSTART' SEP train_name SEP time SEP edge_name SEP distance;

mv_leaves: MV_IND SEP 'LEAVES' SEP train_name SEP time SEP edge_name SEP distance;

mv_term: MV_IND SEP 'TERM' SEP train_name SEP time SEP distance;

ch: 'CH' SEP elem_name SEP STATE SEP time;

msg: 'MSG' SEP node_name SEP time SEP message;

data: 'DATA' SEP train_readable_name SEP time WHITESPACE speed SEP time_with_wrapper;

MV_IND: 'MV';

sw_name: name;
node_name: name;
log_name: name;
node_abs_name: name;
edge_name: name;
edge_abs_name: name;
train_name: name;
kind: name;
speed: INT;
distance: INT;
train_readable_name: (WORD | INT)+;
elem_name: name;
elem_abs_name: name;
name: any+;
coord: INT SEP INT;
name_prefix: '<' INT DOT INT DOT INT '>' COLON;
time: rat | INT;
rat: INT '/' INT;
time_with_wrapper: 'Time' '(' time ')';
message: any+;
any: ~(NEWLINE | SEP);
error_char: .;
value: INT;
bool: 'False' | 'True';


STATE:   NOSIG
       | FAHRT
       | STOP
       ;
NOSIG: 'NOSIG';
FAHRT: 'FAHRT';
STOP: 'STOP';
INT: DIGIT+;
WORD: (LOWERCASE_CHAR | UPPERCASE_CHAR)+;
LOWERCASE_WORD: LOWERCASE_CHAR+;
UPPERCASE_WORD: UPPERCASE_CHAR+;
LOWERCASE_CHAR: ('a' .. 'z');
UPPERCASE_CHAR: ('A' .. 'Z');
DIGIT: ('0' .. '9');
DOT: '.';
SEP: ';';
COLON: ':';
UNDERSCORE: '_';
NEWLINE: '\n' | '\r' | '\r\n';
WHITESPACE: ' ' | '\t';
SIGNAL : 'SIGNAL';