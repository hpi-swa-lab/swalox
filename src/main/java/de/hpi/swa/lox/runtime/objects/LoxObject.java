package de.hpi.swa.lox.runtime.objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectLibrary;

import de.hpi.swa.lox.runtime.LoxContext;
import de.hpi.swa.lox.bytecode.LoxBytecodeRootNode.ReadPropertyNode;
import de.hpi.swa.lox.bytecode.LoxBytecodeRootNode.WritePropertyNode;

@ExportLibrary(InteropLibrary.class)
public final class LoxObject extends DynamicObject {

    public final LoxClass klass;

    public LoxObject(LoxClass klass) {
        super(klass.instanceShape);
        this.klass = klass;
    }

    @Override
    public String toString() {
        return klass.name;
    }

    // Member Messages
    /**
     * Returns <code>true</code> if the receiver may have members. Therefore, at
     * least one of
     * {@link #readMember(Object, String)},
     * {@link #writeMember(Object, String, Object)},
     * {@link #removeMember(Object, String)},
     * {@link #invokeMember(Object, String, Object...)} must
     * not throw {@link UnsupportedMessageException}. Members are structural
     * elements of a class.
     * For example, a method or field is a member of a class. Invoking this message
     * does not cause
     * any observable side-effects. Returns <code>false</code> by default.
     *
     * @see #getMembers(Object, boolean)
     * @see #isMemberReadable(Object, String)
     * @see #isMemberModifiable(Object, String)
     * @see #isMemberInvocable(Object, String)
     * @see #isMemberInsertable(Object, String)
     * @see #isMemberRemovable(Object, String)
     * @see #readMember(Object, String)
     * @see #writeMember(Object, String, Object)
     * @see #removeMember(Object, String)
     * @see #invokeMember(Object, String, Object...)
     * @since 19.0
     */
    @ExportMessage
    public boolean hasMembers() {
        return true;
    }

    @ExportMessage
    public Object getMembers(boolean includeInternal) {
        List<Object> keys = new ArrayList<>();
        keys.addAll(Arrays.asList(DynamicObjectLibrary.getUncached().getKeyArray(this)));
        return LoxContext.get(null).getEnv().asGuestValue(keys);
    }

    @ExportMessage
    public boolean isMemberReadable(String member) {
        return ((ArrayList) LoxContext.get(null).getEnv().asHostObject(getMembers(true))).contains(member);
    }

    @ExportMessage
    public Object readMember(String member, @Cached ReadPropertyNode readNode) {
        return readNode.execute(member, this);

    }

    @ExportMessage
    public boolean isMemberModifiable(String member) {
        return true;
    }

    @ExportMessage
    public boolean isMemberInsertable(String member) {
        return true;
    }

    @ExportMessage
    public void writeMember(String member, Object value, @Cached WritePropertyNode writeNode) {
        writeNode.execute(member, this, value);
    }
}
