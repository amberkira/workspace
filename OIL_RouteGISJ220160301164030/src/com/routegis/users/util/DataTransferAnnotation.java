package com.routegis.users.util;

import core.routegis.engine.avlist.AVKey;
import core.routegis.engine.geom.Position;
import core.routegis.engine.render.*;


public class DataTransferAnnotation extends DialogAnnotation
{
    protected static final String CANCEL_IMAGE_PATH = "com/rotuegis/users/images/16x16-button-cancel.png";

    protected static final String CANCEL_TOOLTIP_TEXT = "È¡Ïû´«Êä";

    protected long position;
    protected long length;
    // Nested annotation components.
    protected Annotation titleLabel;
    protected ButtonAnnotation cancelButton;
    protected Annotation positionLabel;
    protected Annotation lengthLabel;
    protected ProgressAnnotation progress;

    public DataTransferAnnotation(Position position)
    {
        super(position);
        
        this.setClipPosition(0);
        this.setClipLength(0);
    }

    public long getClipPosition()
    {
        return this.position;
    }

    public void setClipPosition(long position)
    {
        this.position = position;

        String text = this.formatTimeString(position);
        this.getClipPositionLabel().setText(text);

        this.getClipProgressBar().setValue(position);
    }

    public long getClipLength()
    {
        return this.length;
    }

    public void setClipLength(long length)
    {
        this.length = length;

        String text = this.formatTimeString(length);
        this.getClipLengthLabel().setText(text);

        this.getClipProgressBar().setMax(0);
        this.getClipProgressBar().setMax(length);
    }

    public Annotation getTitleLabel()
    {
        return this.titleLabel;
    }

    public ButtonAnnotation getCancelButton()
    {
        return this.cancelButton;
    }

    public Annotation getClipPositionLabel()
    {
        return this.positionLabel;
    }

    public Annotation getClipLengthLabel()
    {
        return this.lengthLabel;
    }

    public ProgressAnnotation getClipProgressBar()
    {
        return this.progress;
    }

    //
    //
    //

    protected void initComponents()
    {
        super.initComponents();

        this.titleLabel = new ScreenAnnotation("", new java.awt.Point());
        this.cancelButton = new ButtonAnnotation(CANCEL_IMAGE_PATH, DEPRESSED_MASK_PATH);
        this.positionLabel = new ScreenAnnotation("", new java.awt.Point());
        this.lengthLabel = new ScreenAnnotation("", new java.awt.Point());
        this.progress = new ProgressAnnotation();

        this.setupTitle(this.titleLabel);
        this.setupTimeLabel(this.positionLabel);
        this.setupTimeLabel(this.lengthLabel);
        this.setupProgressBar(this.progress);

        this.cancelButton.setActionCommand(AVKey.STOP);
        this.cancelButton.addActionListener(this);
        this.cancelButton.setToolTipText(CANCEL_TOOLTIP_TEXT);
    }

    protected void layoutComponents()
    {
        super.layoutComponents();

        Annotation controlsContainer = new ScreenAnnotation("", new java.awt.Point());
        {
            this.setupContainer(controlsContainer);
            controlsContainer.setLayout(new AnnotationFlowLayout(AVKey.HORIZONTAL, AVKey.CENTER, 4, 0)); // hgap, vgap
            controlsContainer.addChild(this.cancelButton);
            controlsContainer.addChild(this.positionLabel);
            controlsContainer.addChild(this.progress);
            controlsContainer.addChild(this.lengthLabel);

            java.awt.Insets insets = this.positionLabel.getAttributes().getInsets();
            this.positionLabel.getAttributes().setInsets(
                new java.awt.Insets(insets.top, insets.left + 4, insets.bottom, insets.right));
        }

        Annotation contentContainer = new ScreenAnnotation("", new java.awt.Point());
        {
            this.setupContainer(contentContainer);
            contentContainer.setLayout(new AnnotationFlowLayout(AVKey.VERTICAL, AVKey.CENTER, 0, 16)); // hgap, vgap
            contentContainer.addChild(this.titleLabel);
            contentContainer.addChild(controlsContainer);
        }

        this.addChild(contentContainer);
    }

    protected void setupTitle(Annotation annotation)
    {
        this.setupLabel(annotation);

        AnnotationAttributes attribs = annotation.getAttributes();
        attribs.setFont(java.awt.Font.decode("Arial-BOLD-14"));
        attribs.setSize(new java.awt.Dimension(260, 0));
        attribs.setTextAlign(AVKey.CENTER);
    }

    protected void setupTimeLabel(Annotation annotation)
    {
        this.setupLabel(annotation);

        AnnotationAttributes attribs = annotation.getAttributes();
        attribs.setFont(java.awt.Font.decode("CourierNew-PLAIN-12"));
        attribs.setSize(new java.awt.Dimension(80, 0));
    }

    protected void setupProgressBar(ProgressAnnotation annotation)
    {
        AnnotationAttributes defaultAttribs = new AnnotationAttributes();
        this.setupDefaultAttributes(defaultAttribs);
        defaultAttribs.setSize(new java.awt.Dimension(160, 10));
        annotation.getAttributes().setDefaults(defaultAttribs);
    }

    //
    //
    //

    protected String formatTimeString(long millis)
    {
        return this.formatAsMinutesSeconds(millis);
    }

    protected String formatAsMinutesSeconds(long millis)
    {
        long minutes = (millis / 1000) / 60;
        long seconds = (millis / 1000);
        long remainderSeconds = seconds - minutes * 60;

        return String.format("%02d:%02d", minutes, remainderSeconds);
    }
}
