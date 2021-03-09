package tv.athena.live.barrage.render.shader;

class ShaderCode {
    protected static final String VSH_CODE =
            "attribute vec4 position_vertex;" +
                    "attribute vec2 texture_vertex;" +
                    "varying vec2 texture_coord;" +
                    "uniform mat4 projection_view_matrix;" +
                    "uniform mat4 model_matrix;" +
                    "void main() {" +
                    "gl_Position = projection_view_matrix *  model_matrix * position_vertex;" +
                    "texture_coord = texture_vertex;" +
                    "}";

    protected static final String FSH_CODE =
            "precision highp float;" +
                    "varying vec2 texture_coord;" +
                    "uniform float alpha;" +
                    "uniform sampler2D texture_sampler;" +
                    "void main() {" +
                    "gl_FragColor = texture2D(texture_sampler, texture_coord);" +
                    "if(gl_FragColor.a < 0.001)" +
                    "discard;" +
                    "gl_FragColor.a *= alpha;" +
                    "}";

    protected static final String POSITION = "position_vertex";
    protected static final String TEXTURE = "texture_vertex";
    protected static final String PROJECTION_VIEW_MATRIX = "projection_view_matrix";
    protected static final String MODEL_MATRIX = "model_matrix";
    protected static final String SAMPLER = "texture_sampler";
    protected static final String ALPHA = "alpha";
}
