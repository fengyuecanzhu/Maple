package me.fycz.maple;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author fengyue
 * @date 2022/3/28 14:52
 */
public class MapleBridge {

    public Member target;
    public Method backup;
    public MethodHookParam param;
    public MethodHook callback;

    private MapleBridge() {
    }

    private native Method doHook(Member original, Method callback);

    private native boolean doUnhook(Member target);

    public Object callback(Object[] args) throws Throwable {
        param = new MethodHookParam();
        param.method = backup;
        if (Modifier.isStatic(target.getModifiers())) {
            param.thisObject = null;
            param.args = args;
        } else {
            param.thisObject = args[0];
            param.args = new Object[args.length - 1];
            System.arraycopy(args, 1, param.args, 0, args.length - 1);
        }
        // call "before method" callbacks
        try {
            callback.beforeHookedMethod(param);
        } catch (Throwable t) {
            MapleUtils.log(t);
            // reset result (ignoring what the unexpectedly exiting callback did)
            param.setResult(null);
            param.returnEarly = false;
        }
        // call original method if not requested otherwise
        if (!param.returnEarly) {
            try {
                param.setResult(backup.invoke(param.thisObject, param.args));
            } catch (InvocationTargetException e) {
                param.setThrowable(e.getCause());
            }
        }
        // call "after method" callbacks
        Object lastResult = param.getResult();
        Throwable lastThrowable = param.getThrowable();
        try {
            callback.afterHookedMethod(param);
        } catch (Throwable t) {
            MapleUtils.log(t);
            // reset to last result (ignoring what the unexpectedly exiting callback did)
            if (lastThrowable == null)
                param.setResult(lastResult);
            else
                param.setThrowable(lastThrowable);
        }
        // return
        if (param.hasThrowable())
            throw param.getThrowable();
        else {
            var result = param.getResult();
            if (target instanceof Method) {
                var returnType = ((Method) target).getReturnType();
                if (!returnType.isPrimitive())
                    return returnType.cast(result);
            }
            return result;
        }
    }

    public static MapleBridge hookMethod(Member target, MethodHook callback) {
        MapleBridge bridge = new MapleBridge();
        try {
            var callbackMethod = MapleBridge.class.getDeclaredMethod("callback", Object[].class);
            var result = bridge.doHook(target, callbackMethod);
            if (result == null) return null;
            bridge.backup = result;
            bridge.target = target;
            bridge.callback = callback;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bridge;
    }

    /**
     * Wraps information about the method call and allows to influence it.
     */
    public static final class MethodHookParam {

        /**
         * The hooked method/constructor backup.
         */
        public Member method;

        /**
         * The {@code this} reference for an instance method, or {@code null} for static methods.
         */
        public Object thisObject;

        /**
         * Arguments to the method call.
         */
        public Object[] args;

        private Object result = null;
        private Throwable throwable = null;
        public boolean returnEarly = false;

        /**
         * Returns the result of the method call.
         */
        public Object getResult() {
            return result;
        }

        /**
         * Modify the result of the method call.
         *
         * <p>If called from {@link MethodHook#beforeHookedMethod}, it prevents the call to the original method.
         */
        public void setResult(Object result) {
            this.result = result;
            this.throwable = null;
            this.returnEarly = true;
        }

        /**
         * Returns the {@link Throwable} thrown by the method, or {@code null}.
         */
        public Throwable getThrowable() {
            return throwable;
        }

        /**
         * Returns true if an exception was thrown by the method.
         */
        public boolean hasThrowable() {
            return throwable != null;
        }

        /**
         * Modify the exception thrown of the method call.
         *
         * <p>If called from {@link MethodHook#beforeHookedMethod}, it prevents the call to the original method.
         */
        public void setThrowable(Throwable throwable) {
            this.throwable = throwable;
            this.result = null;
            this.returnEarly = true;
        }

        /**
         * Returns the result of the method call, or throws the Throwable caused by it.
         */
        public Object getResultOrThrowable() throws Throwable {
            if (throwable != null)
                throw throwable;
            return result;
        }
    }
}
