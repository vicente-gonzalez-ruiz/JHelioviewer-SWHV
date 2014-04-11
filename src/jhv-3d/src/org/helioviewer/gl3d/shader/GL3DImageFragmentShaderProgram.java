package org.helioviewer.gl3d.shader;

import javax.media.opengl.GL;

import org.helioviewer.viewmodel.view.opengl.shader.GLFragmentShaderProgram;
import org.helioviewer.viewmodel.view.opengl.shader.GLShaderBuilder;
import org.helioviewer.viewmodel.view.opengl.shader.GLShaderBuilder.GLBuildShaderException;

public class GL3DImageFragmentShaderProgram extends GLFragmentShaderProgram {

    private double cutOffRadius= 0.0f;
	private double xTextureScale;
	private double yTextureScale;

	public GL3DImageFragmentShaderProgram() {
    }
    /**
     * Binds (= activates it) the shader, if it is not active so far.
     * 
     * @param gl
     *            Valid reference to the current gl object
     */
    public final void bind(GL gl) {
    	bind(gl, shaderID, cutOffRadius, xTextureScale, yTextureScale);
    }

    /**
     * Pushes the shader currently in use onto a stack.
     * 
     * This is useful to load another shader but still being able to restore the
     * old one, similar to the very common pushMatrix() in OpenGL.
     * 
     * @param gl
     *            Valid reference to the current gl object
     * @see #popShader(GL)
     */
    public static void pushShader(GL gl) {
        shaderStack.push(shaderCurrentlyUsed);
        gl.glPushAttrib(GL.GL_CURRENT_BIT);
        // Log.debug("GL3DFragmentShaderProgram: pushShader, current="+shaderCurrentlyUsed);
    }

    /**
     * Takes the top of from the shader stack and binds it.
     * 
     * This restores a shader pushed onto the stack earlier, similar to the very
     * common popMatrix() in OpenGL.
     * 
     * @param gl
     *            Valid reference to the current gl object
     * @see #pushShader(GL)
     */
    public static void popShader(GL gl) {
        gl.glPopAttrib();
        Integer restoreShaderObject = shaderStack.pop();
        int restoreShader = restoreShaderObject == null ? 0 : restoreShaderObject.intValue();
        if (restoreShader >= 0) {
            bind(gl, restoreShader, 0.0f,0.0,0.0);
        }
    }

    /**
     * Binds (= activates it) the given shader, if it is not active so far.
     * 
     * @param gl
     *            Valid reference to the current gl object
     */
    private static void bind(GL gl, int shader, double cutOffRadius, double xTextureScale, double yTextureScale) {
        if (shader != shaderCurrentlyUsed) {
            shaderCurrentlyUsed = shader;
            gl.glBindProgramARB(target, shader);
            gl.glProgramLocalParameter4dARB(target, 0, cutOffRadius, 0.0f, 0.0f, 0.0f);
            gl.glProgramLocalParameter4dARB(target, 1,xTextureScale, yTextureScale, 0, 0);
        }
    }
    protected void buildImpl(GLShaderBuilder shaderBuilder) {
        try {
        	String program = "\tif(texcoord0.x<0.0||texcoord0.y<0.0||texcoord0.x>textureScale.x||texcoord0.y>textureScale.y){"
        			+ "\t\tOUT.color = float4(1.0,0.0,0.0,1.0);" + GLShaderBuilder.LINE_SEP
        			+ "\t}";
            program += "OUT.color.a=texcoord0.z;" + GLShaderBuilder.LINE_SEP;
            program += "OUT.depth=-100;" + GLShaderBuilder.LINE_SEP;      	
        	//program += "\tfloat2 texture;" + GLShaderBuilder.LINE_SEP;
            //program += "\ttexture.x = textureCoordinate.z - 0.5;" + GLShaderBuilder.LINE_SEP;
            //program += "\ttexture.y = textureCoordinate.w - 0.5;" + GLShaderBuilder.LINE_SEP;
            //program += "\toutput.a *= step(length(texture),cutOffRadius);" + GLShaderBuilder.LINE_SEP;
            shaderBuilder.addEnvParameter("float cutOffRadius");
            shaderBuilder.addEnvParameter("float4 textureScale");            

            program = program.replace("output", shaderBuilder.useOutputValue("float4", "COLOR"));
            program = program.replace("textureCoordinate", shaderBuilder.useStandardParameter("float4", "TEXCOORD0"));
            shaderBuilder.useOutputValue("float4", "DEPTH");
            shaderBuilder.addMainFragment(program);
            System.out.println("GL3D Image Fragment Shader:\n" + shaderBuilder.getCode());
        } catch (GLBuildShaderException e) {
            e.printStackTrace();
        }

    }
    
	public void changeTextureScale(double xTextureScale, double yTextureScale) {
		this.xTextureScale = xTextureScale;
		this.yTextureScale = yTextureScale;
	}
	
    public void setCutOffRadius(double cutOffRadius){
    	this.cutOffRadius = cutOffRadius;
    }

}
