package de.hpi.swa.lox.bytecode;

import java.util.ArrayList;
import java.util.function.BiConsumer;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.bytecode.TagTreeNode;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.NodeLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import de.hpi.swa.lox.runtime.LoxContext;

@ExportLibrary(value = NodeLibrary.class, receiverType = TagTreeNode.class)
final class LoxBytecodeScopeExports {

    @ExportMessage
    static boolean hasScope(TagTreeNode node, Frame frame) {
        return true;
    }

    @ExportMessage
    @SuppressWarnings("unused")
    static Object getScope(TagTreeNode node, Frame frame, boolean nodeEnter) throws UnsupportedMessageException {
        return new MapScope(node, frame);
    }

    @ExportLibrary(InteropLibrary.class)
    static class MapScope implements TruffleObject {

        private TagTreeNode node;
        private Frame frame;

        public MapScope(TagTreeNode node, Frame frame) {
            this.node = node;
            this.frame = frame;
        }

        @ExportMessage
        boolean isScope() {
            return true;
        }

        @ExportMessage
        boolean hasMembers() {
            return true;
        }

        @ExportMessage
        Object readMember(String member) throws UnknownIdentifierException {
            Object[] result = new Object[1];
            forEachMember((s, i) -> {
                if (member.equals(s)) {
                    result[0] = frame.getValue(i);
                }
            });
            if (result[0] != null) {
                return result[0];
            }
            throw UnknownIdentifierException.create(member);
        }

        void forEachMember(BiConsumer<String, Integer> c) {
            for (var local : node.getBytecodeNode().getLocals()) {
                var name = local.getName();
                if (name instanceof String s) {
                    c.accept(s, local.getLocalIndex());
                }
            }
        }

        @ExportMessage
        final Object getMembers(boolean includeInternal) throws UnsupportedMessageException {
            var members = new ArrayList<String>();
            forEachMember((s, i) -> members.add(s));
            return LoxContext.get(null).getEnv().asGuestValue(members);
        }

        @ExportMessage
        final boolean isMemberReadable(String member) {
            Boolean result[] = { false };
            forEachMember((s, i) -> {
                if (member.equals(s)) {
                    if (frame.getFrameDescriptor().getSlotKind(i) != FrameSlotKind.Illegal) {
                        result[0] = true;
                    }
                }
            });
            return result[0];
        }

        @ExportMessage
        final boolean hasLanguage() {
            return false;
        }

        @ExportMessage
        final Class<? extends TruffleLanguage<?>> getLanguage() throws UnsupportedMessageException {
            return null;
        }

        @ExportMessage
        final Object toDisplayString(boolean allowSideEffects) {
            return "MapScope";
        }

    }
}