(jde-load-master-project "Data.Master")	; check for alternate ../prj.el
(jde-set-project-name "JSettlers")
(jde-set-variables 
; '(jde-read-compile-args nil)
 '(jde-global-classpath (quote ( "C:/Data/Programs/jsettlers-1.0.6-src/java/src" ".")) t)
; '(jde-compile-option-debug (quote ("selected" (t nil nil))))
 '(jde-compile-option-command-line-args  "-source 1.4")
; '(jde-compile-option-nowarn nil)
; '(jde-compile-option-verbose nil)
; '(jde-compile-option-verbose-path nil)
; '(jde-compile-option-directory "C:/Data/Programs/jsettlers-1.0.6-src/java/src/target/lib" t)
; '(jde-compile-option-classpath "C:/Data/Programs/jsettlers-1.0.6-src/java/src/target/lib")
; '(jde-compile-option-extdirs nil)
; '(jde-compile-option-sourcepath nil)
; '(jde-compile-option-depend-switch (quote ("-Xdepend")))
; '(jde-compile-option-target (quote ("1.1")))
; '(jde-compile-option-debug (quote ("all" (t t t))) t)

 )
