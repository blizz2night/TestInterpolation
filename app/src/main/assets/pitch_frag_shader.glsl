//#extension GL_OES_EGL_image_external : require
precision mediump float;

//uniform samplerExternalOES u_TextureUnit;
uniform sampler2D u_TextureUnit;

varying vec2 v_TexCoord;
uniform float u_Border;

void main() {
    float x = v_TexCoord.x;
    float x_grey;
    if(u_Border>0.5){
        x_grey = (16.0 + x * 16.0) / 48.0;
    }else{
        x_grey = (16.0 + x * 15.0 + 0.5 ) / 48.0;
    }

    gl_FragColor = texture2D(u_TextureUnit, vec2(x_grey,v_TexCoord.y));
}