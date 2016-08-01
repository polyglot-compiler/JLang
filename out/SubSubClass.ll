%class.SimpleClass = type {%dv.SimpleClass*, i32, i16}
%dv.java.lang.Object = type {i8*, i1 (%class.java.lang.Object*, %class.java.lang.Object*)*, %class.java.lang.Class* (%class.java.lang.Object*)*, i32 (%class.java.lang.Object*)*, void (%class.java.lang.Object*)*, void (%class.java.lang.Object*)*, %class.java.lang.String* (%class.java.lang.Object*)*, void (%class.java.lang.Object*, i64)*, void (%class.java.lang.Object*, i64, i32)*, void (%class.java.lang.Object*)*, %class.java.lang.Object* (%class.java.lang.Object*)*, void (%class.java.lang.Object*)*}
%dv.SimpleClass = type {i8*, i1 (%class.SimpleClass*, %class.java.lang.Object*)*, %class.java.lang.Class* (%class.SimpleClass*)*, i32 (%class.SimpleClass*)*, void (%class.SimpleClass*)*, void (%class.SimpleClass*)*, %class.java.lang.String* (%class.SimpleClass*)*, void (%class.SimpleClass*, i64)*, void (%class.SimpleClass*, i64, i32)*, void (%class.SimpleClass*)*, %class.java.lang.Object* (%class.SimpleClass*)*, void (%class.SimpleClass*)*, i32 (%class.SimpleClass*)*, i32 (%class.SimpleClass*)*}
%class.java.lang.Class = type opaque
%dv.SubSubClass = type {i8*, i1 (%class.SubSubClass*, %class.java.lang.Object*)*, %class.java.lang.Class* (%class.SubSubClass*)*, i32 (%class.SubSubClass*)*, void (%class.SubSubClass*)*, void (%class.SubSubClass*)*, %class.java.lang.String* (%class.SubSubClass*)*, void (%class.SubSubClass*, i64)*, void (%class.SubSubClass*, i64, i32)*, void (%class.SubSubClass*)*, %class.java.lang.Object* (%class.SubSubClass*)*, void (%class.SubSubClass*)*, i32 (%class.SubSubClass*)*, i32 (%class.SubSubClass*)*, i32 (%class.SubSubClass*)*}
%dv.UseSimpleClass = type {i8*, i1 (%class.UseSimpleClass*, %class.java.lang.Object*)*, %class.java.lang.Class* (%class.UseSimpleClass*)*, i32 (%class.UseSimpleClass*)*, void (%class.UseSimpleClass*)*, void (%class.UseSimpleClass*)*, %class.java.lang.String* (%class.UseSimpleClass*)*, void (%class.UseSimpleClass*, i64)*, void (%class.UseSimpleClass*, i64, i32)*, void (%class.UseSimpleClass*)*, %class.java.lang.Object* (%class.UseSimpleClass*)*, void (%class.UseSimpleClass*)*, i32 (%class.UseSimpleClass*)*, i32 (%class.UseSimpleClass*)*, i32 (%class.UseSimpleClass*)*}
%class.SubSubClass = type {%dv.SubSubClass*, i32, i16}
%class.UseSimpleClass = type {%dv.UseSimpleClass*, i32, i16}
%class.java.lang.Object = type {%dv.java.lang.Object*}
%class.java.lang.String = type opaque
@_J_size_11SubSubClass = global i64 0
@_J_dv_11SubSubClass = global %dv.SubSubClass zeroinitializer
@_J_size_14UseSimpleClass = external global i64
@_J_dv_14UseSimpleClass = external global %dv.UseSimpleClass
@_J_size_11SimpleClass = external global i64
@_J_dv_11SimpleClass = external global %dv.SimpleClass
@_J_size_16java.lang.Object = external global i64
@_J_dv_16java.lang.Object = external global %dv.java.lang.Object
%__ctortype = type { i32, void ()*, i8* }
@llvm.global_ctors = appending global [1 x %__ctortype] [%__ctortype { i32 65535, void ()* @_J_init_11SubSubClass, i8* null }]
declare i8* @malloc(i64 %size)

declare void @_J_11SimpleClass_5print_i32(i32 %arg_0)

declare i32 @_J_14UseSimpleClass_7method2_void(%class.UseSimpleClass* %arg_0)

declare i32 @_J_16java.lang.Object_8hashCode_void(%class.java.lang.Object* %arg_0)

declare void @_J_init_14UseSimpleClass()

define void @main() {
%flat$9 = alloca i32, i32 1
%flat$7 = alloca i32, i32 1
%s = alloca %class.UseSimpleClass*, i32 1
%flat$8 = alloca i32, i32 1
%x = alloca %class.SimpleClass*, i32 1
%flat$11 = alloca i32, i32 1
%flat$10 = alloca i32, i32 1
%flat$12 = alloca i32, i32 1
store %class.UseSimpleClass* null, %class.UseSimpleClass** %s
%_temp.128 = call i8* @malloc(i64 16)
%_temp.129 = bitcast i8* %_temp.128 to %class.SubSubClass*
%_temp.130 = getelementptr %class.SubSubClass, %class.SubSubClass* %_temp.129, i32 0, i32 0
store %dv.SubSubClass* @_J_dv_11SubSubClass, %dv.SubSubClass** %_temp.130
%_temp.131 = bitcast %class.SubSubClass* %_temp.129 to %class.UseSimpleClass*
store %class.UseSimpleClass* %_temp.131, %class.UseSimpleClass** %s
%_temp.132 = load %class.UseSimpleClass*, %class.UseSimpleClass** %s
%_temp.136 = getelementptr %class.UseSimpleClass, %class.UseSimpleClass* %_temp.132, i32 0, i32 1
%_temp.135 = zext i16 65 to i32
store i32 %_temp.135, i32* %_temp.136
%_temp.137 = load %class.UseSimpleClass*, %class.UseSimpleClass** %s
%_dvDoublePtrResult.2 = getelementptr %class.UseSimpleClass, %class.UseSimpleClass* %_temp.137, i32 0, i32 0
%_dvPtrValue.2 = load %dv.UseSimpleClass*, %dv.UseSimpleClass** %_dvDoublePtrResult.2
%_temp.138 = getelementptr %dv.UseSimpleClass, %dv.UseSimpleClass* %_dvPtrValue.2, i32 0, i32 12
%_temp.139 = load i32 (%class.UseSimpleClass*)*, i32 (%class.UseSimpleClass*)** %_temp.138
%_temp.140 = call i32 %_temp.139(%class.UseSimpleClass* %_temp.137)
store i32 %_temp.140, i32* %flat$7
%_temp.141 = load %class.UseSimpleClass*, %class.UseSimpleClass** %s
%_dvDoublePtrResult.3 = getelementptr %class.UseSimpleClass, %class.UseSimpleClass* %_temp.141, i32 0, i32 0
%_dvPtrValue.3 = load %dv.UseSimpleClass*, %dv.UseSimpleClass** %_dvDoublePtrResult.3
%_temp.142 = getelementptr %dv.UseSimpleClass, %dv.UseSimpleClass* %_dvPtrValue.3, i32 0, i32 14
%_temp.143 = load i32 (%class.UseSimpleClass*)*, i32 (%class.UseSimpleClass*)** %_temp.142
%_temp.144 = call i32 %_temp.143(%class.UseSimpleClass* %_temp.141)
store i32 %_temp.144, i32* %flat$8
%_temp.145 = load i32, i32* %flat$7
%_temp.235 = alloca i32, i32 1
store i32 %_temp.145, i32* %_temp.235
%_temp.146 = load i32, i32* %flat$8
%_temp.236 = load i32, i32* %_temp.235
%_temp.147 = add i32 %_temp.236, %_temp.146
store i32 %_temp.147, i32* %flat$9
%_temp.148 = load i32, i32* %flat$9
%_temp.237 = alloca i32, i32 1
store i32 %_temp.148, i32* %_temp.237
%_temp.238 = load i32, i32* %_temp.237
call void @_J_11SimpleClass_5print_i32(i32 %_temp.238)
store %class.SimpleClass* null, %class.SimpleClass** %x
%_temp.151 = load %class.UseSimpleClass*, %class.UseSimpleClass** %s
%_temp.152 = bitcast %class.UseSimpleClass* %_temp.151 to %class.SimpleClass*
store %class.SimpleClass* %_temp.152, %class.SimpleClass** %x
%_temp.153 = load %class.SimpleClass*, %class.SimpleClass** %x
%_dvDoublePtrResult.4 = getelementptr %class.SimpleClass, %class.SimpleClass* %_temp.153, i32 0, i32 0
%_dvPtrValue.4 = load %dv.SimpleClass*, %dv.SimpleClass** %_dvDoublePtrResult.4
%_temp.154 = getelementptr %dv.SimpleClass, %dv.SimpleClass* %_dvPtrValue.4, i32 0, i32 12
%_temp.155 = load i32 (%class.SimpleClass*)*, i32 (%class.SimpleClass*)** %_temp.154
%_temp.156 = call i32 %_temp.155(%class.SimpleClass* %_temp.153)
store i32 %_temp.156, i32* %flat$10
%_temp.157 = load i32, i32* %flat$10
%_temp.239 = alloca i32, i32 1
store i32 %_temp.157, i32* %_temp.239
%_temp.240 = load i32, i32* %_temp.239
call void @_J_11SimpleClass_5print_i32(i32 %_temp.240)
%_temp.159 = load %class.UseSimpleClass*, %class.UseSimpleClass** %s
%_dvDoublePtrResult.5 = getelementptr %class.UseSimpleClass, %class.UseSimpleClass* %_temp.159, i32 0, i32 0
%_dvPtrValue.5 = load %dv.UseSimpleClass*, %dv.UseSimpleClass** %_dvDoublePtrResult.5
%_temp.160 = getelementptr %dv.UseSimpleClass, %dv.UseSimpleClass* %_dvPtrValue.5, i32 0, i32 3
%_temp.161 = load i32 (%class.UseSimpleClass*)*, i32 (%class.UseSimpleClass*)** %_temp.160
%_temp.162 = call i32 %_temp.161(%class.UseSimpleClass* %_temp.159)
store i32 %_temp.162, i32* %flat$11
%_temp.163 = load i32, i32* %flat$11
%_temp.241 = alloca i32, i32 1
store i32 %_temp.163, i32* %_temp.241
%_temp.242 = load i32, i32* %_temp.241
call void @_J_11SimpleClass_5print_i32(i32 %_temp.242)
%_temp.165 = load %class.UseSimpleClass*, %class.UseSimpleClass** %s
%_dvDoublePtrResult.6 = getelementptr %class.UseSimpleClass, %class.UseSimpleClass* %_temp.165, i32 0, i32 0
%_dvPtrValue.6 = load %dv.UseSimpleClass*, %dv.UseSimpleClass** %_dvDoublePtrResult.6
%_temp.166 = getelementptr %dv.UseSimpleClass, %dv.UseSimpleClass* %_dvPtrValue.6, i32 0, i32 3
%_temp.167 = load i32 (%class.UseSimpleClass*)*, i32 (%class.UseSimpleClass*)** %_temp.166
%_temp.168 = call i32 %_temp.167(%class.UseSimpleClass* %_temp.165)
store i32 %_temp.168, i32* %flat$12
%_temp.169 = load i32, i32* %flat$12
%_temp.243 = alloca i32, i32 1
store i32 %_temp.169, i32* %_temp.243
%_temp.244 = load i32, i32* %_temp.243
call void @_J_11SimpleClass_5print_i32(i32 %_temp.244)
ret void
}

define i32 @_J_11SubSubClass_7method2_void(%class.SubSubClass* %_this) {
%flat$14 = alloca i32, i32 1
%flat$13 = alloca i32, i32 1
%_temp.172 = bitcast i32 (%class.UseSimpleClass*)* @_J_14UseSimpleClass_7method2_void to i32 (%class.SubSubClass*)*
%_temp.173 = call i32 %_temp.172(%class.SubSubClass* %_this)
store i32 %_temp.173, i32* %flat$13
%_temp.174 = load i32, i32* %flat$13
%_temp.175 = sub i32 %_temp.174, 10
store i32 %_temp.175, i32* %flat$14
%_temp.176 = load i32, i32* %flat$14
ret i32 %_temp.176
}

define i32 @_J_11SubSubClass_8hashCode_void(%class.SubSubClass* %_this) {
%flat$16 = alloca i32, i32 1
%flat$15 = alloca i32, i32 1
%_temp.178 = bitcast i32 (%class.java.lang.Object*)* @_J_16java.lang.Object_8hashCode_void to i32 (%class.SubSubClass*)*
%_temp.179 = call i32 %_temp.178(%class.SubSubClass* %_this)
store i32 %_temp.179, i32* %flat$15
%_temp.180 = load i32, i32* %flat$15
%_temp.181 = add i32 %_temp.180, 4
store i32 %_temp.181, i32* %flat$16
%_temp.182 = load i32, i32* %flat$16
ret i32 %_temp.182
}

define void @_J_init_11SubSubClass() {
call void @_J_init_14UseSimpleClass()
%_temp.183 = getelementptr %dv.SubSubClass, %dv.SubSubClass* @_J_dv_11SubSubClass, i32 0, i32 0
%_temp.184 = bitcast %dv.UseSimpleClass* @_J_dv_14UseSimpleClass to i8*
store i8* %_temp.184, i8** %_temp.183
%_temp.185 = getelementptr %dv.UseSimpleClass, %dv.UseSimpleClass* @_J_dv_14UseSimpleClass, i32 0, i32 1
%_temp.187 = load i1 (%class.UseSimpleClass*, %class.java.lang.Object*)*, i1 (%class.UseSimpleClass*, %class.java.lang.Object*)** %_temp.185
%_temp.188 = bitcast i1 (%class.UseSimpleClass*, %class.java.lang.Object*)* %_temp.187 to i1 (%class.SubSubClass*, %class.java.lang.Object*)*
%_temp.186 = getelementptr %dv.SubSubClass, %dv.SubSubClass* @_J_dv_11SubSubClass, i32 0, i32 1
store i1 (%class.SubSubClass*, %class.java.lang.Object*)* %_temp.188, i1 (%class.SubSubClass*, %class.java.lang.Object*)** %_temp.186
%_temp.189 = getelementptr %dv.UseSimpleClass, %dv.UseSimpleClass* @_J_dv_14UseSimpleClass, i32 0, i32 2
%_temp.191 = load %class.java.lang.Class* (%class.UseSimpleClass*)*, %class.java.lang.Class* (%class.UseSimpleClass*)** %_temp.189
%_temp.192 = bitcast %class.java.lang.Class* (%class.UseSimpleClass*)* %_temp.191 to %class.java.lang.Class* (%class.SubSubClass*)*
%_temp.190 = getelementptr %dv.SubSubClass, %dv.SubSubClass* @_J_dv_11SubSubClass, i32 0, i32 2
store %class.java.lang.Class* (%class.SubSubClass*)* %_temp.192, %class.java.lang.Class* (%class.SubSubClass*)** %_temp.190
%_temp.193 = getelementptr %dv.SubSubClass, %dv.SubSubClass* @_J_dv_11SubSubClass, i32 0, i32 3
store i32 (%class.SubSubClass*)* @_J_11SubSubClass_8hashCode_void, i32 (%class.SubSubClass*)** %_temp.193
%_temp.194 = getelementptr %dv.UseSimpleClass, %dv.UseSimpleClass* @_J_dv_14UseSimpleClass, i32 0, i32 4
%_temp.196 = load void (%class.UseSimpleClass*)*, void (%class.UseSimpleClass*)** %_temp.194
%_temp.197 = bitcast void (%class.UseSimpleClass*)* %_temp.196 to void (%class.SubSubClass*)*
%_temp.195 = getelementptr %dv.SubSubClass, %dv.SubSubClass* @_J_dv_11SubSubClass, i32 0, i32 4
store void (%class.SubSubClass*)* %_temp.197, void (%class.SubSubClass*)** %_temp.195
%_temp.198 = getelementptr %dv.UseSimpleClass, %dv.UseSimpleClass* @_J_dv_14UseSimpleClass, i32 0, i32 5
%_temp.200 = load void (%class.UseSimpleClass*)*, void (%class.UseSimpleClass*)** %_temp.198
%_temp.201 = bitcast void (%class.UseSimpleClass*)* %_temp.200 to void (%class.SubSubClass*)*
%_temp.199 = getelementptr %dv.SubSubClass, %dv.SubSubClass* @_J_dv_11SubSubClass, i32 0, i32 5
store void (%class.SubSubClass*)* %_temp.201, void (%class.SubSubClass*)** %_temp.199
%_temp.202 = getelementptr %dv.UseSimpleClass, %dv.UseSimpleClass* @_J_dv_14UseSimpleClass, i32 0, i32 6
%_temp.204 = load %class.java.lang.String* (%class.UseSimpleClass*)*, %class.java.lang.String* (%class.UseSimpleClass*)** %_temp.202
%_temp.205 = bitcast %class.java.lang.String* (%class.UseSimpleClass*)* %_temp.204 to %class.java.lang.String* (%class.SubSubClass*)*
%_temp.203 = getelementptr %dv.SubSubClass, %dv.SubSubClass* @_J_dv_11SubSubClass, i32 0, i32 6
store %class.java.lang.String* (%class.SubSubClass*)* %_temp.205, %class.java.lang.String* (%class.SubSubClass*)** %_temp.203
%_temp.206 = getelementptr %dv.UseSimpleClass, %dv.UseSimpleClass* @_J_dv_14UseSimpleClass, i32 0, i32 7
%_temp.208 = load void (%class.UseSimpleClass*, i64)*, void (%class.UseSimpleClass*, i64)** %_temp.206
%_temp.209 = bitcast void (%class.UseSimpleClass*, i64)* %_temp.208 to void (%class.SubSubClass*, i64)*
%_temp.207 = getelementptr %dv.SubSubClass, %dv.SubSubClass* @_J_dv_11SubSubClass, i32 0, i32 7
store void (%class.SubSubClass*, i64)* %_temp.209, void (%class.SubSubClass*, i64)** %_temp.207
%_temp.210 = getelementptr %dv.UseSimpleClass, %dv.UseSimpleClass* @_J_dv_14UseSimpleClass, i32 0, i32 8
%_temp.212 = load void (%class.UseSimpleClass*, i64, i32)*, void (%class.UseSimpleClass*, i64, i32)** %_temp.210
%_temp.213 = bitcast void (%class.UseSimpleClass*, i64, i32)* %_temp.212 to void (%class.SubSubClass*, i64, i32)*
%_temp.211 = getelementptr %dv.SubSubClass, %dv.SubSubClass* @_J_dv_11SubSubClass, i32 0, i32 8
store void (%class.SubSubClass*, i64, i32)* %_temp.213, void (%class.SubSubClass*, i64, i32)** %_temp.211
%_temp.214 = getelementptr %dv.UseSimpleClass, %dv.UseSimpleClass* @_J_dv_14UseSimpleClass, i32 0, i32 9
%_temp.216 = load void (%class.UseSimpleClass*)*, void (%class.UseSimpleClass*)** %_temp.214
%_temp.217 = bitcast void (%class.UseSimpleClass*)* %_temp.216 to void (%class.SubSubClass*)*
%_temp.215 = getelementptr %dv.SubSubClass, %dv.SubSubClass* @_J_dv_11SubSubClass, i32 0, i32 9
store void (%class.SubSubClass*)* %_temp.217, void (%class.SubSubClass*)** %_temp.215
%_temp.218 = getelementptr %dv.UseSimpleClass, %dv.UseSimpleClass* @_J_dv_14UseSimpleClass, i32 0, i32 10
%_temp.220 = load %class.java.lang.Object* (%class.UseSimpleClass*)*, %class.java.lang.Object* (%class.UseSimpleClass*)** %_temp.218
%_temp.221 = bitcast %class.java.lang.Object* (%class.UseSimpleClass*)* %_temp.220 to %class.java.lang.Object* (%class.SubSubClass*)*
%_temp.219 = getelementptr %dv.SubSubClass, %dv.SubSubClass* @_J_dv_11SubSubClass, i32 0, i32 10
store %class.java.lang.Object* (%class.SubSubClass*)* %_temp.221, %class.java.lang.Object* (%class.SubSubClass*)** %_temp.219
%_temp.222 = getelementptr %dv.UseSimpleClass, %dv.UseSimpleClass* @_J_dv_14UseSimpleClass, i32 0, i32 11
%_temp.224 = load void (%class.UseSimpleClass*)*, void (%class.UseSimpleClass*)** %_temp.222
%_temp.225 = bitcast void (%class.UseSimpleClass*)* %_temp.224 to void (%class.SubSubClass*)*
%_temp.223 = getelementptr %dv.SubSubClass, %dv.SubSubClass* @_J_dv_11SubSubClass, i32 0, i32 11
store void (%class.SubSubClass*)* %_temp.225, void (%class.SubSubClass*)** %_temp.223
%_temp.226 = getelementptr %dv.UseSimpleClass, %dv.UseSimpleClass* @_J_dv_14UseSimpleClass, i32 0, i32 12
%_temp.228 = load i32 (%class.UseSimpleClass*)*, i32 (%class.UseSimpleClass*)** %_temp.226
%_temp.229 = bitcast i32 (%class.UseSimpleClass*)* %_temp.228 to i32 (%class.SubSubClass*)*
%_temp.227 = getelementptr %dv.SubSubClass, %dv.SubSubClass* @_J_dv_11SubSubClass, i32 0, i32 12
store i32 (%class.SubSubClass*)* %_temp.229, i32 (%class.SubSubClass*)** %_temp.227
%_temp.230 = getelementptr %dv.UseSimpleClass, %dv.UseSimpleClass* @_J_dv_14UseSimpleClass, i32 0, i32 13
%_temp.232 = load i32 (%class.UseSimpleClass*)*, i32 (%class.UseSimpleClass*)** %_temp.230
%_temp.233 = bitcast i32 (%class.UseSimpleClass*)* %_temp.232 to i32 (%class.SubSubClass*)*
%_temp.231 = getelementptr %dv.SubSubClass, %dv.SubSubClass* @_J_dv_11SubSubClass, i32 0, i32 13
store i32 (%class.SubSubClass*)* %_temp.233, i32 (%class.SubSubClass*)** %_temp.231
%_temp.234 = getelementptr %dv.SubSubClass, %dv.SubSubClass* @_J_dv_11SubSubClass, i32 0, i32 14
store i32 (%class.SubSubClass*)* @_J_11SubSubClass_7method2_void, i32 (%class.SubSubClass*)** %_temp.234
ret void
}

